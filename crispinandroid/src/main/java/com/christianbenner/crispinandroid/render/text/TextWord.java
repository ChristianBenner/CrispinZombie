package com.christianbenner.crispinandroid.render.text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christian Benner on 27/11/2017.
 */

public class TextWord
{
    private List<TextChar> characters = new ArrayList<TextChar>();
    private double width = 0;
    private double fontSize;

    public TextWord(double fontSize)
    {
        this.fontSize = fontSize;
    }

    public void addCharacter(TextChar character)
    {
        characters.add(character);
        width += character.getxAdvance() * fontSize;
    }

    public List<TextChar> getCharacters()
    {
        return characters;
    }

    public double getWordWidth()
    {
        return width;
    }
}
