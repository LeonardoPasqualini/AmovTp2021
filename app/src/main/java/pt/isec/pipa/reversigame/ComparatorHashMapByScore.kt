package pt.isec.pipa.reversigame

import android.util.Log
import java.util.Comparator

// classe para comprar os hash maps usando o score como base de comparacao
// usado para ordenar o array de score
class ComparatorHashMapByScore: Comparator<HashMap<String, Any?>> {

    override fun compare(o1: HashMap<String, Any?>?, o2: HashMap<String, Any?>?): Int {
        Log.i("onActualizaDados", "o1: ${o1}; o2: ${o2}; ")
        if(o1 == null || o2 == null ||
            o1.get("score") == null || o2.get("score") == null){
            return 0;
        }
        return ((o1.get("score") as Long).compareTo(o2.get("score") as Long))*-1
    }

}