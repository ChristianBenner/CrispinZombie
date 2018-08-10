package com.christianbenner.crispinandroid.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christian Benner on 27/11/2017.
 */

public class TextLine
{
    private double maxLength;
    private double spaceSize;

    private List<Word> words = new ArrayList<Word>();
    private double currentLineLength;

    public TextLine(double spaceWidth, double fontSize, double maxLength)
    {
        this.spaceSize = spaceWidth * fontSize;
        this.maxLength = maxLength;
    }

    public boolean addWord(Word word)
    {
        double additionalLength = word.getWordWidth();
        additionalLength += !words.isEmpty() ? spaceSize : 0;
        if(currentLineLength + additionalLength <= maxLength)
        {
            words.add(word);
            currentLineLength += additionalLength;
            return true;
        }
        else
        {
            return false;
        }
    }

    public double getMaxLength()
    {
        return maxLength;
    }

    public double getLineLength()
    {
        return currentLineLength;
    }

    public List<Word> getWords()
    {
        return words;
    }
}
