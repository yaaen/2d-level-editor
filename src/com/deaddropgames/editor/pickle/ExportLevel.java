package com.deaddropgames.editor.pickle;

import com.deaddropgames.editor.elements.Tree;

import java.util.ArrayList;

public class ExportLevel extends BaseLevel {

    // member variables
    private ArrayList<PolyLine> polyLines;
    private ArrayList<Tree> trees;

    public ExportLevel() {

        super();

        polyLines = new ArrayList<>();
        trees = new ArrayList<>();
    }

    public ArrayList<PolyLine> getPolyLines() {

        return polyLines;
    }

    public ArrayList<Tree> getTrees() {

        return trees;
    }

    public void copyMetaData(final Level level) {

        name = level.getName();
        description = level.getDescription();
        author = level.getAuthor();
        difficulty = level.getDifficulty();
        if(level.getEndX() > 0f) {

            endX = level.getEndX();
        }
    }
}
