package com.christianbenner.crispinandroid.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christian Benner on 27/11/2017.
 */

public class Word
{
    private List<GLCharacter> characters = new ArrayList<GLCharacter>();
    private double width = 0;
    private double fontSize;

    public Word(double fontSize)
    {
        this.fontSize = fontSize;
    }

    public void addCharacter(GLCharacter character)
    {
        characters.add(character);
        width += character.getxAdvance() * fontSize;
    }

    public List<GLCharacter> getCharacters()
    {
        return characters;
    }

    public double getWordWidth()
    {
        return width;
    }
}
