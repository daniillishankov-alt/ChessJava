package com.example.demo;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.event.ActionEvent;
import javafx.scene.Node;

public class PrototypeChess {

    @FXML private GridPane chessGrid;

    private final char bKing = '♚', bQueen = '♛', bRook = '♜', bBishop = '♝', bKnight = '♞', bPawn = '♟';
    private final char wKing = '♔', wQueen = '♕', wRook = '♖', wBishop = '♗', wKnight = '♘', wPawn = '♙';

    private final char[][] gameField = new char[8][8];
    private Button selectedButton = null;
    private int sourceCol = -1, sourceRow = -1;
    private boolean isWhiteTurn = true;

    private int enPassantTargetCol = -1;
    private int enPassantTargetRow = -1;

    private boolean wKingMoved = false;
    private boolean wRookLeftMoved = false;
    private boolean wRookRightMoved = false;
    private boolean bKingMoved = false;
    private boolean bRookLeftMoved = false;
    private boolean bRookRightMoved = false;

    @FXML
    void initialize() {
        chessGrid.setAlignment(Pos.CENTER);
        createBoard();
        restartGame();
    }

    private void createBoard() {
        chessGrid.getChildren().clear();
        chessGrid.getRowConstraints().clear();
        chessGrid.getColumnConstraints().clear();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Button btn = new Button();

                btn.setMinWidth(75); btn.setMaxWidth(75); btn.setPrefWidth(75); btn.setMaxWidth(Double.MAX_VALUE);
                btn.setMinHeight(75); btn.setMaxHeight(75); btn.setPrefHeight(75); btn.setMaxHeight(Double.MAX_VALUE);
                btn.setPadding(Insets.EMPTY);
                btn.setFocusTraversable(false);
                btn.setOnAction(this::btnClick);

                chessGrid.add(btn, col, row);
            }
        }
    }

    @FXML
    void restartGame() {
        setChessLogic();
        isWhiteTurn = true;
        selectedButton = null;
        enPassantTargetCol = -1;
        enPassantTargetRow = -1;

        wKingMoved = false; wRookLeftMoved = false; wRookRightMoved = false;
        bKingMoved = false; bRookLeftMoved = false; bRookRightMoved = false;

        refreshUI();
    }

//    pieces arrangement
    private void setChessLogic() {
        for(int i=0; i<8; i++) for(int j=0; j<8; j++) gameField[i][j] = 0;

        for (int i = 0; i < 8; i++) gameField[i][1] = bPawn;
        gameField[0][0] = gameField[7][0] = bRook;
        gameField[1][0] = gameField[6][0] = bKnight;
        gameField[2][0] = gameField[5][0] = bBishop;
        gameField[3][0] = bQueen; gameField[4][0] = bKing;

        for (int i = 0; i < 8; i++) gameField[i][6] = wPawn;
        gameField[0][7] = gameField[7][7] = wRook;
        gameField[1][7] = gameField[6][7] = wKnight;
        gameField[2][7] = gameField[5][7] = wBishop;
        gameField[3][7] = wQueen; gameField[4][7] = wKing;
    }

    private void refreshUI() {
        int[] wKPos = findKing(true);
        int[] bKPos = findKing(false);
        boolean wCheck = isSquareAttacked(wKPos[0], wKPos[1], false);
        boolean bCheck = isSquareAttacked(bKPos[0], bKPos[1], true);

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Button btn = getButtonFromGrid(i, j);
                if (btn != null) {
                    char piece = gameField[i][j];
                    String bgColor = ((i + j) % 2 == 0) ? "#7A6354FF" : "#5e3c2d";
                    String textColor = isWhitePiece(piece) ? "#eee" : "#252525";

                    if (piece == wKing && wCheck) bgColor = "#ff8a80";
                    if (piece == bKing && bCheck) bgColor = "#ff8a80";

                    btn.setStyle("-fx-background-color: " + bgColor + "; " +
                            "-fx-background-insets: 0; -fx-background-radius: 0; " +
                            "-fx-text-fill: " + textColor + "; -fx-font-size: 50px; -fx-cursor: hand;");
                    btn.setText(piece == 0 ? "" : String.valueOf(piece));
                }
            }
        }
    }

    @FXML
    void btnClick(ActionEvent event) {
        Button clickedBtn = (Button) event.getSource();
        int col = getCol(clickedBtn);
        int row = getRow(clickedBtn);
        char pieceAtClick = gameField[col][row];

        if (selectedButton == null) {
            if (pieceAtClick != 0 && isWhiteTurn == isWhitePiece(pieceAtClick)) {
                selectedButton = clickedBtn;
                sourceCol = col; sourceRow = row;
                selectedButton.setStyle(selectedButton.getStyle() + "-fx-background-color: #9f9f42;");
                highlightMoves(sourceCol, sourceRow, pieceAtClick);
            }
        } else {
            if (selectedButton == clickedBtn) { refreshUI(); selectedButton = null; return; }
            char movingPiece = gameField[sourceCol][sourceRow];

            if (isValidMove(sourceCol, sourceRow, col, row, movingPiece) && isKingSafeAfterMove(sourceCol, sourceRow, col, row, isWhiteTurn)) {

                if ((movingPiece == wPawn || movingPiece == bPawn) && col == enPassantTargetCol && row == (isWhitePiece(movingPiece) ? enPassantTargetRow - 1 : enPassantTargetRow + 1)) {
                    gameField[enPassantTargetCol][enPassantTargetRow] = 0;
                }

                if ((movingPiece == wKing || movingPiece == bKing) && Math.abs(col - sourceCol) == 2) {
                    int rookSourceCol = (col == 6) ? 7 : 0;
                    int rookTargetCol = (col == 6) ? 5 : 3;
                    gameField[rookTargetCol][row] = gameField[rookSourceCol][row];
                    gameField[rookSourceCol][row] = 0;
                }

                if (movingPiece == wKing) wKingMoved = true;
                if (movingPiece == bKing) bKingMoved = true;
                if (movingPiece == wRook && sourceCol == 0 && sourceRow == 7) wRookLeftMoved = true;
                if (movingPiece == wRook && sourceCol == 7 && sourceRow == 7) wRookRightMoved = true;
                if (movingPiece == bRook && sourceCol == 0 && sourceRow == 0) bRookLeftMoved = true;
                if (movingPiece == bRook && sourceCol == 7 && sourceRow == 0) bRookRightMoved = true;

                enPassantTargetCol = -1; enPassantTargetRow = -1;
                if ((movingPiece == wPawn || movingPiece == bPawn) && Math.abs(row - sourceRow) == 2) {
                    enPassantTargetCol = col; enPassantTargetRow = row;
                }

                gameField[col][row] = movingPiece;
                gameField[sourceCol][sourceRow] = 0;

                if (gameField[col][row] == wPawn && row == 0) gameField[col][row] = wQueen;
                if (gameField[col][row] == bPawn && row == 7) gameField[col][row] = bQueen;

                isWhiteTurn = !isWhiteTurn;
                refreshUI();
                checkGameStatus();
            } else {
                refreshUI();
            }
            selectedButton = null;
        }
    }

    private boolean isValidMove(int sCol, int sRow, int tCol, int tRow, char piece) {
        if (sCol == tCol && sRow == tRow) return false;
        char target = gameField[tCol][tRow];
        if (target != 0 && isWhitePiece(target) == isWhitePiece(piece)) return false;
        int dCol = Math.abs(tCol - sCol); int dRow = Math.abs(tRow - sRow);

//  Rook & Queen
        if ((piece == wRook || piece == bRook || piece == wQueen || piece == bQueen) && (sCol == tCol || sRow == tRow)) {
            int cs = Integer.compare(tCol, sCol), rs = Integer.compare(tRow, sRow);
            int c = sCol + cs, r = sRow + rs;
            while (c != tCol || r != tRow) { if (gameField[c][r] != 0) return false; c += cs; r += rs; }
            return true;
        }

//  Bishop & Queen
        if ((piece == wBishop || piece == bBishop || piece == wQueen || piece == bQueen) && dCol == dRow) {
            int cs = Integer.compare(tCol, sCol), rs = Integer.compare(tRow, sRow);
            int c = sCol + cs, r = sRow + rs;
            while (c != tCol || r != tRow) { if (gameField[c][r] != 0) return false; c += cs; r += rs; }
            return true;
        }

//  Knight
        if (piece == wKnight || piece == bKnight) return (dCol == 1 && dRow == 2) || (dCol == 2 && dRow == 1);

//  King
        if (piece == wKing || piece == bKing) {
            if (dCol <= 1 && dRow <= 1) return true;

            if (sRow == tRow && dCol == 2 && dRow == 0) {
                boolean isWhite = isWhitePiece(piece);
                if (isWhite && wKingMoved) return false;
                if (!isWhite && bKingMoved) return false;

                if (isSquareAttacked(sCol, sRow, !isWhite)) return false;

                if (tCol == 6) {
                    if (isWhite && wRookRightMoved) return false;
                    if (!isWhite && bRookRightMoved) return false;
                    if (gameField[5][sRow] != 0 || gameField[6][sRow] != 0) return false;

                    if (isSquareAttacked(5, sRow, !isWhite) || isSquareAttacked(6, sRow, !isWhite)) return false;
                    return true;
                }
                if (tCol == 2) {
                    if (isWhite && wRookLeftMoved) return false;
                    if (!isWhite && bRookLeftMoved) return false;
                    if (gameField[1][sRow] != 0 || gameField[2][sRow] != 0 || gameField[3][sRow] != 0) return false;

                    if (isSquareAttacked(3, sRow, !isWhite) || isSquareAttacked(2, sRow, !isWhite)) return false;
                    return true;
                }
            }
            return false;
        }

//  Pawn
        if (piece == wPawn || piece == bPawn) {
            int dir = isWhitePiece(piece) ? -1 : 1;
            if (tCol == sCol && tRow == sRow + dir && target == 0) return true;
            if (tCol == sCol && sRow == (isWhitePiece(piece)?6:1) && tRow == sRow + 2*dir && target == 0 && gameField[sCol][sRow+dir] == 0) return true;
            if (dCol == 1 && tRow == sRow + dir && target != 0) return true;
            if (dCol == 1 && tRow == sRow + dir && target == 0 && tCol == enPassantTargetCol && sRow == enPassantTargetRow) return true;
        }
        return false;
    }

    private boolean isSquareAttacked(int col, int row, boolean byWhite) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                char p = gameField[i][j];
                if (p != 0 && isWhitePiece(p) == byWhite) {
                    int dCol = Math.abs(col - i);
                    int dRow = Math.abs(row - j);

                    if (p == wPawn || p == bPawn) {
                        int dir = isWhitePiece(p) ? -1 : 1;
                        if (dCol == 1 && row == j + dir) return true;
                    } else if (p == wKing || p == bKing) {
                        if (dCol <= 1 && dRow <= 1) return true;
                    } else {
                        if (isValidMove(i, j, col, row, p)) return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isKingSafeAfterMove(int sc, int sr, int tc, int tr, boolean white) {
        char moving = gameField[sc][sr]; char target = gameField[tc][tr];

        if ((moving == wKing || moving == bKing) && Math.abs(tc - sc) == 2) return true;

        gameField[tc][tr] = moving; gameField[sc][sr] = 0;
        int[] kp = findKing(white);
        boolean safe = !isSquareAttacked(kp[0], kp[1], !white);
        gameField[sc][sr] = moving; gameField[tc][tr] = target;
        return safe;
    }

    private void checkGameStatus() {
        int[] kingPos = findKing(isWhiteTurn);
        boolean inCheck = isSquareAttacked(kingPos[0], kingPos[1], !isWhiteTurn);
        boolean hasMoves = false;
        outer: for (int i = 0; i < 8; i++) for (int j = 0; j < 8; j++) {
            char p = gameField[i][j];
            if (p != 0 && isWhitePiece(p) == isWhiteTurn) {
                for (int ti = 0; ti < 8; ti++) for (int tj = 0; tj < 8; tj++) {
                    if (isValidMove(i, j, ti, tj, p) && isKingSafeAfterMove(i, j, ti, tj, isWhiteTurn)) {
                        hasMoves = true; break outer;
                    }
                }
            }
        }
        if (!hasMoves) {
            new Alert(Alert.AlertType.INFORMATION, inCheck ? "Schaakmat!" : "Pat!").showAndWait();
            restartGame();
        }
    }

    private void highlightMoves(int sc, int sr, char p) {
        for (int i = 0; i < 8; i++) for (int j = 0; j < 8; j++) {
            if (isValidMove(sc, sr, i, j, p) && isKingSafeAfterMove(sc, sr, i, j, isWhitePiece(p))) {
                Button btn = getButtonFromGrid(i, j);
                if (btn != null) {
                    String baseColor = ((i + j) % 2 == 0) ? "#eeeed2" : "#769656";
                    String highlightColor = (gameField[i][j] == 0) ? "rgba(129,199,132,0.6)" : "rgba(229,115,115,0.7)";

                    btn.setStyle("-fx-background-color: " + baseColor + ", " + highlightColor + "; " +
                            "-fx-background-insets: 0, 0; -fx-background-radius: 0; " +
                            "-fx-text-fill: " + (isWhitePiece(gameField[i][j]) ? "#ffffff" : "#000000") + "; " +
                            "-fx-font-size: 50px; -fx-cursor: hand;");
                }
            }
        }
    }

    private int[] findKing(boolean white) {
        char k = white ? wKing : bKing;
        for (int i = 0; i < 8; i++) for (int j = 0; j < 8; j++) if (gameField[i][j] == k) return new int[]{i, j};
        return new int[]{0,0};
    }

    private boolean isWhitePiece(char p) { return "♔♕♖♗♘♙".indexOf(p) >= 0; }
    private int getCol(Button b) { Integer c = GridPane.getColumnIndex(b); return c == null ? 0 : c; }
    private int getRow(Button b) { Integer r = GridPane.getRowIndex(b); return r == null ? 0 : r; }
    private Button getButtonFromGrid(int col, int row) {
        for (Node node : chessGrid.getChildren()) {
            if (node instanceof Button b && getCol(b) == col && getRow(b) == row) return b;
        }
        return null;
    }
}
