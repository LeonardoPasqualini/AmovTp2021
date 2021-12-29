package pt.isec.pipa.reversigame

class UserScore (scoreParam : Long?, guestNameParam : String?, guestScoreParam : Long?){

    var score : Long? = scoreParam;
    var guestName : String? = guestNameParam;
    var guestScore : Long? = guestScoreParam;

    override fun toString(): String {

        return "USER SCORE  Score: ${score}; guestName: ${guestName};guestScore: ${guestScore};"
    }
}