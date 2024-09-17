package PongDemo

import de.fabmax.kool.util.Time

class GameLogic(var ball: Ball, var paddle1:PongPaddle, var paddle2: PongPaddle) {

    var player1Score=0
    var player2Score=0

    var numberOfBounces=0

    var shootTimer=1.5f
    var respawn=false

    fun update(){
        respawnBall()
        updateBallSpeed()

        paddle1.update()
        paddle2.update()
    }

    fun updateBallSpeed(){
        when(numberOfBounces){
            10-> ball.body.maxLinearVelocity=ball.maxVelocity2
            20-> ball.body.maxLinearVelocity=ball.maxVelocity3
        }
    }

    fun respawnBall(){
        if(respawn){
            ball.resetPosition()
            shootTimer-=Time.deltaT
            if(shootTimer<=0){
                respawn=false
                ball.shootBall()
                shootTimer=1.5f
            }
        }
    }
}