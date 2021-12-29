package pt.isec.pipa.reversigame

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_perfil.*
import pt.isec.pipa.reversigame.databinding.ActivityPerfilBinding
import pt.isec.pipa.reversigame.databinding.ActivityTopScoreBinding
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.net.URI
import java.util.regex.Matcher
import java.util.regex.Pattern


class PerfilActivity : AppCompatActivity() {
    private val TAG = "PerfilActivity" // limitado a 23 char
    lateinit var b : ActivityPerfilBinding
    private var imagePathURI : Uri? = null
    private var imagePath2 : String? = null
    private val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    private val REQ_PERM_CODE = 1000
    private var permissionsGranted = false
    private var username : String? = null


    private lateinit var auth: FirebaseAuth

    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(b.root)

        auth = Firebase.auth

//        val gso =
//            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(google_web_id) // bug do android studio, a var está la e compila bem
//                .requestEmail() //acesso ao email para mostrar
//                .build()
//        googleSignInClient = GoogleSignIn.getClient(this,gso)

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.CAMERA), REQ_PERM_CODE)
        } else
            permissionsGranted = true
    }

    override fun onStart() {
        super.onStart()
        showUser(auth.currentUser) //se estiver null não existe user autenticado
    }


    fun showUser(user : FirebaseUser?) {
        val str = when (user) {
            null -> "Username"
            else -> "${user.email}"
        }
        if(user != null)
            username = str
        b.tvUsername.text = str
        Log.i(TAG,str)
    }

    fun createUserWithEmail(email:String, password:String) {
        val isEmai = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        if(password.length<6){
            Toast.makeText(baseContext, "Password must have at leas 6 characters", Toast.LENGTH_SHORT).show()
            return
        } else if(!isValidPassword(password)){
            Toast.makeText(baseContext, " Password must at least 1 Alphabet, 1 Number and 1 Special Character", Toast.LENGTH_SHORT).show()
            return
        }

        //pode-se usar o addOnCompletListener que pode ou não dar certo.
        // Neste exemplo falha e sucesso está dividido. Uma das funçoes pode fazer estouras
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(this) { result ->
                Log.i(TAG, "createUser: success")
                onCriarDados()
                showUser(auth.currentUser)
                Toast.makeText(baseContext, "${username} successfully created", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener(this) { e ->
                Log.i(TAG, "createUser: failure ${e.message}")
                showUser(null)
                Toast.makeText(baseContext, "Something went wrong, please try again", Toast.LENGTH_SHORT).show()
            }
    }
    // funcao para validar password
    fun isValidPassword(password: String?): Boolean {
        val pattern: Pattern
        //regex para ter pelo menos 1 char 1 numero e 1 especial
        val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$"
        pattern = Pattern.compile(PASSWORD_PATTERN)
        val matcher: Matcher = pattern.matcher(password)
        return matcher.matches()
    }

    fun signInWithEmail(email:String,password:String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(this) { result ->
                Log.d(TAG, "signInWithEmail: success")
                showUser(auth.currentUser)
            }
            .addOnFailureListener(this) { e->
                Log.d(TAG, "signInWithEmail: failure ${e.message}")
                showUser(null)
            }
    }

    fun onCriarDados() {
        val db = Firebase.firestore
        val username = auth.currentUser?.email
        if(username == null){
            return
        }
        var user = UserScore(0, "None", 0)

        var arr = ArrayList<UserScore>()
        arr.add(user)

        var scores = hashMapOf("topScore" to arr)

        db.collection("Modo2").document(username).set(scores)
        db.collection("Modo3").document(username).set(scores)

//        db.collection("TopScore").get()
//            .addOnSuccessListener { result -> //para mostrar todos os documentos
//                for(doc in result.documents){
//                    Log.i(TAG, "onCrearDados: ${doc.id}")
//                }
//            }
    }

    // lança uma segunda atividade autenticação google
    val signInWithGoogle = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult() // primeira fase, pega o token gerado no mail
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
            firebaseAuthWithGoogle(account.idToken!!) // manda para o firebase o token do mail
        } catch (e: ApiException) {
            Log.i(TAG, "onActivityResult - Google authentication: failure")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener(this) { result ->
                Log.d(TAG, "signInWithCredential:success")
                showUser(auth.currentUser)
            }
            .addOnFailureListener(this) { e ->
                Log.d(TAG, "signInWithCredential:failure ${e.message}")
                showUser(auth.currentUser)
            }
    }

//    fun onRegistarEmail(view: android.view.View) {
//        //fazer verificações de input null e outras
//        createUserWithEmail(b.edEmail.text.toString(), b.edPassword.text.toString())
//    }

    fun onAutenticarEmail(view: android.view.View) {
        signInWithEmail(b.edEmail.text.toString(), b.edPassword.text.toString())
        b.edEmail.setText("")
        b.edPassword.setText("")
    }

    fun onRegistarEmail(view: android.view.View) {
        createUserWithEmail(b.edEmail.text.toString(), b.edPassword.text.toString())
        b.edEmail.setText("")
        b.edPassword.setText("")
    }

    fun onAutenticarGmail(view: android.view.View) {
        Log.i("onActualizaDados", "onAutenticarGmail")
        onActualizarDados()
        //signInWithGoogle.launch(googleSignInClient.signInIntent) // tem uma propriedade que ja cria o intent
    }

    fun takePic(view: android.view.View) {
        imagePath2 = SimpleDateFormat(FILENAME_FORMAT, Locale.ENGLISH)
            .format(System.currentTimeMillis()) + ".jpg"

        val intent = Intent(this, CameraActivity::class.java)
        intent.putExtra("imagePath", imagePath2)
        startActivityForResult.launch(intent)
    }

    var startActivityForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult() ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            val resultIntent = result.data
            val imagePath = resultIntent?.getStringExtra("path")
            imagePathURI = Uri.parse(imagePath)

            Log.i("Perfil", "resultIntent photoFile: ${imagePath2}")
//            val uri = resultIntent?.data?.apply {
//                val cursor = contentResolver.query(this,
//                    arrayOf(MediaStore.Images.ImageColumns.DATA), null, null, null)
//                if (cursor !=null && cursor.moveToFirst())
//                imagePath = cursor.getString(0)
//            }
            if(imagePath == null) {
                b.perfilImage?.background = ResourcesCompat.getDrawable(resources,
                    android.R.drawable.ic_menu_report_image, null)
            } else {
//                ImageUtils.setPic(b.perfilImage, imagePathURI!!)
                val bitmap = BitmapFactory.decodeFile(imagePath)
                Log.i("Perfil", "bitmap: ${bitmap} ${imagePath}")
//                b.perfilImage.setImageURI(null);
                b.perfilImage.setImageURI(imagePathURI);
//                b.perfilImage?.setImageBitmap(bitmap)
            }
        }
    }

    fun onActualizarDados() {
        if( username == null)
            return

        val db = Firebase.firestore
        Log.i("onActualizaDados", "user name : ${auth.currentUser}")
        val v = db.collection("Modo2").document(username.toString())
        Log.i("onActualizaDados", "v: ${v}")
        /* v.get(Source.SERVER) //SERVER copia direto do servidor; CACHE copia armazenada no dispositivo
             .addOnSuccessListener {
                 v.update("nrgames",it.getLong("nrgames")!!+1)
             }*/
        // para quando haja vários acessos na bd
        db.runTransaction {transaction ->
            val doc = transaction.get(v)
            Log.i("onActualizaDados", "doc: ${doc}")
            var nUser = ArrayList<HashMap<String, Any?>>()
            var currentUserScore = UserScore( 5000000, "ola" ,60);

            //Caso consiga prgar o topScore, instyancia o nUser
            if(doc.get("topScore") != null){
                nUser = doc.get("topScore") as ArrayList<HashMap<String, Any?>>
            }
            //Se o user não tiver 5 jogos feitos, guarda direto
            if(nUser.size < 5){
                //cria um novo topScore
                nUser.add(hashMapOf(
                    "guestName" to "lala",
                    "guestScore" to 1,
                    "score" to 55
                ))
                //atualiza bd
                transaction.update(v,"topScore", nUser)
            } else {
                // ordena asc
                var sortednUser = nUser.sortedWith(ComparatorHashMapByScore())
                //Compara o menor escore com o scor do jogo atual
                // TODO ainda precisa vir o valor do jogo
                var lastIndex = sortednUser.size-1
                if(sortednUser[lastIndex].get("score") == null){

                }
                if( sortednUser[lastIndex].get("score") == null ||
                    (sortednUser[lastIndex].get("score") as Long) < currentUserScore.score!!
                ){
                    sortednUser[lastIndex].put("guestName", currentUserScore.guestName)
                    sortednUser[lastIndex].put("guestScore", currentUserScore.guestScore)
                    sortednUser[lastIndex].put("score", currentUserScore.score)

                    transaction.update(v,"topScore", sortednUser)
                }
            }

            null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_PERM_CODE) {
            permissionsGranted =
                (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
        }
    }

    fun onSignOut(view: android.view.View) {
//        onActualizarDados()

        if (auth.currentUser != null) {

            auth.signOut()
        } else {
            googleSignInClient.signOut()
                .addOnCompleteListener(this) {
                    showUser(null)
                }
        }
        username = null
        showUser(auth.currentUser)
    }


}