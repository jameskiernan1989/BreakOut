import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JPanel;

public class Board extends JPanel implements Commons{
    private Timer timer;
    private String message = "Game Over";
    private Ball ball;
    private Paddle paddle;
    private Brick bricks[];
    private boolean ingame = true;

    public Board(){
        initBoard();
    }

    private void initBoard(){
        addKeyListener(new TAdapter());
        setFocusable(true);
        bricks = new Brick[N_OF_BRICKS];
        setDoubleBuffered(true);
        timer = new Timer();
        timer.scheduleAtFixedRate(new ScheduleTask(), DELAY, PERIOD);
    }

    @Override
    public void addNotify(){
        super.addNotify();
        gameInit();
    }

    private void gameInit(){
        /**
         * We create a ball, a paddle, and thirty bricks.
         */
        ball = new Ball();
        paddle = new Paddle();

        int k = 0;
        for(int i =0; i < 5; i++){
            for(int j = 0; j < 6; j++){
                bricks[k] = new Brick(j * 40 + 30, i * 10 + 50);
                k++;
            }
        }
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        /**
         * Depending on the ingame variable, we either draw all the objects in the
         * drawObjects() method or finish the game with the gameFinished() method.
         */
        if(ingame){
            drawObjects(g2d);
        }else{
            gameFinished(g2d);
        }
        Toolkit.getDefaultToolkit().sync();
    }

    private void drawObjects(Graphics2D g2d){
        /**
         * Draws all the objects of the game. The sprites are drawn with the drawImage()
         * method.
         */
        g2d.drawImage(ball.getImage(), ball.getX(), ball.getY(),
                ball.getWidth(), ball.getHeight(), this);
        g2d.drawImage(paddle.getImage(), paddle.getX(), paddle.getY(),
                paddle.getWidth(), paddle.getHeight(), this);

        for(int i = 0; i < N_OF_BRICKS; i++){
            if(!bricks[i].isDestroyed()){
                g2d.drawImage(bricks[i].getImage(), bricks[i].getX(),
                        bricks[i].getY(), bricks[i].getWidth(),
                        bricks[i].getHeight(), this);
            }
        }
    }

    private void gameFinished(Graphics2D g2d){
        /**
         * Draws "Game Over" or "Victory" to the middle of the window.
         */
        Font font = new Font("Verdana", Font.BOLD, 18);
        FontMetrics metr = this.getFontMetrics(font);

        g2d.setColor(Color.BLACK);
        g2d.setFont(font);
        g2d.drawString(message, (Commons.WIDTH - metr.stringWidth(message)) / 2,
                Commons.WIDTH / 2);
    }

    private class TAdapter extends KeyAdapter{
        @Override
        public void keyReleased(KeyEvent e){
            paddle.keyReleased(e);
        }
        @Override
        public void keyPressed(KeyEvent e){
            paddle.keyPressed(e);
        }
    }

    private class ScheduleTask extends TimerTask{
        /**
         * Triggered every PERIOD ms. In its run() method, we move the ball and
         * the paddle. We check for possible collisions and repaints the screen.
         */
        @Override
        public void run(){
            ball.move();
            paddle.move();
            checkCollision();
            repaint();
        }
    }

    private void stopGame(){
        ingame = false;
        timer.cancel();
    }

    private void checkCollision(){
        if(ball.getRect().getMaxY() > Commons.BOTTOM_EDGE){
            /**
             * If the ball hits the bottom, we stop the game.
             */
            stopGame();
        }

        for(int i = 0, j = 0; i < N_OF_BRICKS; i++){
            /**
             * We check how many bricks are destroyed. If we destroyed all N_OF_BRICKS,
             * we win the game.
             */
            if(bricks[i].isDestroyed()){
                j++;
            }

            if(j == N_OF_BRICKS){
                message = "Victory";
                stopGame();
            }
        }
        if((ball.getRect()).intersects(paddle.getRect())){
            int paddleLPos = (int) paddle.getRect().getMinX();
            int ballLPos = (int)ball.getRect().getMinX();

            int first = paddleLPos + 8;
            int second = paddleLPos + 16;
            int third = paddleLPos + 24;
            int fourth = paddleLPos + 32;

            /**
             * If the ball hits the first part of the paddle, we change the direction of
             * the ball to the north west.
             */
            if(ballLPos < first){
                ball.setXDir(-1);
                ball.setYDir(-1);
            }
            if(ballLPos >= first && ballLPos < second){
                ball.setXDir(-1);
                ball.setYDir(-1 * ball.getYDir());
            }
            if(ballLPos >= second && ballLPos < third){
                ball.setXDir(0);
                ball.setYDir(-1);
            }
            if(ballLPos >= third && ballLPos < fourth){
                ball.setXDir(1);
                ball.setYDir(-1 * ball.getYDir());
            }
            if(ballLPos > fourth){
                ball.setXDir(1);
                ball.setYDir(-1);
            }
        }

        for(int i = 0; i < N_OF_BRICKS; i++){
            if((ball.getRect()).intersects(bricks[i].getRect())){
                int ballLeft = (int) ball.getRect().getMinX();
                int ballHeight = (int)ball.getRect().getHeight();
                int ballWidth = (int)ball.getRect().getWidth();
                int ballTop = (int) ball.getRect().getMinY();

                Point pointRight = new Point(ballLeft + ballWidth + 1, ballTop);
                Point pointLeft = new Point(ballLeft - 1, ballTop);
                Point pointTop = new Point(ballLeft, ballTop - 1);
                Point pointBottom = new Point(ballLeft, ballTop + ballHeight + 1);

                if(!bricks[i].isDestroyed()){
                    if(bricks[i].getRect().contains(pointRight)){
                        ball.setXDir(-1);
                    }else if(bricks[i].getRect().contains(pointTop)){
                        /**
                         * If the ball hits the bottom of the brick, we change the y direction of
                         * the ball; it goes down.
                         */
                        ball.setYDir(-1);
                    }
                    bricks[i].setDestroyed(true);
                }
            }
        }
    }
}