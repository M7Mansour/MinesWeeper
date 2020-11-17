import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MinesWeeper extends Application{
    private static final int SQUARE_SIZE = 40;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    //number of squares in the x , y plane
    private static final int X_SQUARES = WIDTH / SQUARE_SIZE;
    private static final int Y_SQUARES = HEIGHT / SQUARE_SIZE;
    //create minesfeild to store squares in it
    private Square[][] minesField = new Square[X_SQUARES][Y_SQUARES];
    private static int numberOfBombs = 0;
    private static int numberOfOpenBoxes = 0;
    //determine if the game is won or currently playing
    private static boolean isWon = false;
    private static boolean gameOver = false;
    Scene scene ;
    // launch the app
    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception{
        scene = new Scene(createMinesField());
        stage.setScene(scene);
        stage.setTitle("M7Mansour - MinesWeeper");
        stage.show();
    }

    //put the bombs in minesfield
    private Pane createMinesField(){
        //create pane to put minesfield in it
        Pane pane = new Pane();
        pane.setPrefSize(WIDTH,HEIGHT);
        //create squares and put them in the minesfield
        for(int i = 0 ; i < X_SQUARES ; i++)
            for(int j = 0 ; j < Y_SQUARES ; j++){
                //                              if the random number is lower than 0.145 then the square will carry a bomb
                Square square = new Square(i,j,Math.random() < 0.145);
                minesField[i][j] = square;
                pane.getChildren().add(square);
            }
        //fill squares with number of bombs near them
        for(int i = 0 ; i < X_SQUARES ; i++)
            for(int j = 0 ; j < Y_SQUARES ; j++) {
                // hide the value of squares
                minesField[i][j].text.setVisible(false);
                // increment the counter of bombs
                if (minesField[i][j].carryBomb)
                    setBombsNumber(minesField[i][j]);
            }
        return pane;
    }

    // set the number of near bombs in the squares near the bomb
    private void setBombsNumber(Square square){
        // coordinates of squares around the bomb
        int[] near = {-1,-1 , -1,0 , -1,1 , 0,-1 , 0,1 , 1,-1 , 1,0 , 1,1};
        for(int i = 0 ; i < near.length ; i++){
            int x = square.xCord + near[i++];
            int y = square.yCord + near[i];
            if(x >= 0 && x < X_SQUARES && y >= 0 && y < Y_SQUARES) {
                minesField[x][y].nearBombs++;
                if(!minesField[x][y].carryBomb)
                    minesField[x][y].text.setText("" + minesField[x][y].nearBombs);
            }
        }
    }

    // square class that we will use it to create squares to put them in the minesfield
    private class Square extends StackPane{
        private int xCord;
        private int yCord;
        private boolean carryBomb;
        private int nearBombs = 0;
        private boolean isOpen = false;
        // we leave 1.5 pixel in each size for border
        private Rectangle squareBody = new Rectangle(SQUARE_SIZE - 3 , SQUARE_SIZE - 3);
        private Text text = new Text();
        public Square(int xCord , int yCord , boolean carryBomb){
            this.xCord = xCord;
            this.yCord = yCord;
            this.carryBomb = carryBomb;
            squareBody.setStroke(Color.BLACK);
            squareBody.setFill(Color.GREY);
            text.setText(carryBomb ? "X" : "");
            this.text.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 30));
            super.getChildren().addAll(squareBody,text);
            // set the coordinates of square
            super.setTranslateX(xCord * SQUARE_SIZE);
            super.setTranslateY(yCord * SQUARE_SIZE);
            if(carryBomb)
                numberOfBombs++;
            // event listener for mouse click
            setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    open(this);
                }
                else if (event.getButton() == MouseButton.SECONDARY && !isWon && !gameOver && !this.isOpen) {
                    if(this.squareBody.getFill() == Color.GREY)
                        this.squareBody.setFill(Color.BLUE);
                    else this.squareBody.setFill(Color.GREY);
                }
            });

        }


        private void open(Square square){
            // if the game is over either won or lose it will restart when the mouse click on any square
            if(isWon || gameOver){
                isWon = false;
                gameOver = false;
                numberOfBombs = 0;
                numberOfOpenBoxes = 0;
                scene.setRoot(createMinesField());
                return;
            }
            // if a bomb is clicked
            if(square.carryBomb){
                square.isOpen = true;
                gameOver = true;
                // set exploded bomb color to red
                square.squareBody.setFill(Color.RED);
                square.text.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 30));
                for (int i = 0; i < X_SQUARES; i++)
                    for (int j = 0; j < Y_SQUARES; j++) {
                        //set all squares text to visible and color to white
                        minesField[i][j].text.setVisible(true);
                        if(!minesField[i][j].isOpen)
                            minesField[i][j].text.setFill(Color.WHITE);
                        // if the square is bomb set its color to blue
                        if(minesField[i][j].carryBomb && minesField[i][j] != square)
                            minesField[i][j].squareBody.setFill(Color.BLUE);
                        minesField[i][j].text.setVisible(true);
                    }
                    return;
            }
            //if the square is not empty open it
            if (square.nearBombs > 0 && !square.isOpen) {
                square.isOpen = true;
                numberOfOpenBoxes++;
                square.text.setVisible(true);
                square.squareBody.setFill(Color.WHITE);
            }
            // if the square is empty open it and the its near squares
            else if (square.nearBombs == 0 && !square.isOpen) {
                square.isOpen = true;
                numberOfOpenBoxes++;
                square.squareBody.setFill(Color.WHITE);
                openNear(square);
            }
            // if all squares that doesn't have a bomb is opened then the game is won
            if((WIDTH / SQUARE_SIZE) * (HEIGHT / SQUARE_SIZE) - numberOfBombs == numberOfOpenBoxes) {
                wonAction();
            }
        }
        private void wonAction(){
            for (int i = 0; i < X_SQUARES; i++)
                for (int j = 0; j < Y_SQUARES; j++) {
                    // if the square is a bomb set its color to greenyellow
                    if(minesField[i][j].carryBomb)
                        minesField[i][j].squareBody.setFill(Color.GREENYELLOW);
                    //set all squares text to visible
                    minesField[i][j].text.setVisible(true);
                }
            isWon = true;
        }
        // open all near squares of the passed square
        private void openNear(Square square){
            // near coordinates
            int[] near = {-1,-1 , -1,0 , -1,1 , 0,-1 , 0,1 , 1,-1 , 1,0 , 1,1};
            for(int i = 0 ; i < near.length ; i++){
                int x = square.xCord + near[i++];
                int y = square.yCord + near[i];
                // make sure that the square is not outside the minesfield
                if(x >= 0 && x < X_SQUARES && y >= 0 && y < Y_SQUARES)
                    if(!minesField[x][y].carryBomb)
                        open(minesField[x][y]);
            }
        }
    }
}
