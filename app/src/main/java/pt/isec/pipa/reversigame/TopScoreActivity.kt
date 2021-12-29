package pt.isec.pipa.reversigame

import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
import pt.isec.pipa.reversigame.databinding.ActivityTopScoreBinding
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import android.text.method.ScrollingMovementMethod




class TopScoreActivity : AppCompatActivity() {
    private val TAG = "TopScoreActivity" // limitado a 23 char
    lateinit var b : ActivityTopScoreBinding
    private lateinit var auth: FirebaseAuth
    private var google_web_id = "AIzaSyBmzRfZkGB39D_tYDrgLHajOuVVHXw0FUg.apps.googleusercontent.com"
    private lateinit var googleSignInClient: GoogleSignInClient
    private var username : String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityTopScoreBinding.inflate(layoutInflater)

        setContentView(b.root)
        //scroll bar na text view
        b.tvTopScore.movementMethod = ScrollingMovementMethod()

        auth = Firebase.auth

        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(google_web_id) // bug do android studio, a var está la e compila bem
                .requestEmail() //acesso ao email para mostrar
                .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)

//        onCriarDados()
//        onActualizarDados()
        listaResultado()
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

        username = str
        b.tvUsername?.text = str
        Log.i(TAG,str)
    }

    fun listaResultado(){
        val db = Firebase.firestore
        val docList = ArrayList<String>()
        val collection = db.collection("Modo2")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
//                    if(document.get == username)
                    // vem como Map então precisas de cast para ArrayList
                    var teste = document.data["topScore"] as ArrayList<Any?>
                    // O ArrayList é de HashMaps então tem de fazer o cast
                    var score = teste[0] as HashMap<String, Any?>

//                    b.tv.append("Jogador ${document.id} :\n\t${teste[0]}\n\t${teste[1]}\n\t${teste[2]}\n")
                    b.tvTopScore.append("Jogador ${document.id} :\n\t${score["score"]}\n")

//                    if(document.data["Score"] == null){
//                        b.tv.append("Jogador ${document.id} => ${document.data}\n")
//                    } else {
//                        b.tv.append("Jogador ${document.id} => ${document.data}\n")
//                    }

                }
            }
            .addOnFailureListener { exception ->
                Log.w("listaResultado", "Error getting documents: ", exception)
            }
    }
}
