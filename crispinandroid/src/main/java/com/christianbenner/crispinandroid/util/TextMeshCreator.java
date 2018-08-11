package com.christianbenner.crispinandroid.util;

import android.content.Context;

import com.christianbenner.crispinandroid.data.GLCharacter;
import com.christianbenner.crispinandroid.data.TextLine;
import com.christianbenner.crispinandroid.data.TextMesh;
import com.christianbenner.crispinandroid.data.TextMetaFile;
import com.christianbenner.crispinandroid.data.Word;
import com.christianbenner.crispinandroid.ui.GLText;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christian Benner on 27/11/2017.
 */

// This class was not coded in a very readable way to increase efficiency when generating chars,
public class TextMeshCreator
{
    public static final double LINE_HEIGHT = 0.03f;
    public static final int SPACE_ASCII = 32;

    // The data of all the chars
    private TextMetaFile metaData;

    private float minX = 0.0f;
    private float maxX = 0.0f;

    private float totalHeight = 0.0f;
    private float lineHeight = 0.0f;
    private float lineAndOffsetHeight = 0.0f;
    private float maxCharOffset = 0.0f;

    public TextMeshCreator(Context context, int resourceId)
    {
        metaData = new TextMetaFile(context, resourceId);
    }

    public TextMesh createTextMesh(GLText text)
    {
        return createQuadVertices(text, createStructure(text));
    }

    private List<TextLine> createStructure(GLText text)
    {
        List<TextLine> lines = new ArrayList<TextLine>();
        TextLine currentLine = new TextLine(metaData.getSpaceWidth(),
                text.getFontSize(), text.getMaxLineSize());
        Word currentWord = new Word(text.getFontSize());
        for(char c : text.getTextString().toCharArray())
        {
            int ascii = (int)c;
            if(ascii == SPACE_ASCII)
            {
                boolean added = currentLine.addWord(currentWord);
                if(!added)
                {
                    lines.add(currentLine);
                    currentLine = new TextLine(metaData.getSpaceWidth(), text.getFontSize(),
                            text.getMaxLineSize());

                    currentLine.addWord(currentWord);
                }
                currentWord = new Word(text.getFontSize());
                continue;
            }
            currentWord.addCharacter(metaData.getCharacter(ascii));
        }

        completeStructure(lines, currentLine, currentWord, text);
        return lines;
    }

    private void completeStructure(List<TextLine> lines, TextLine currentLine,
                                     Word currentWord, GLText text)
{
    boolean added = currentLine.addWord(currentWord);
    if(!added)
    {
        lines.add(currentLine);
        currentLine = new TextLine(metaData.getSpaceWidth(),
                text.getFontSize(), text.getMaxLineSize());
        currentLine.addWord(currentWord);
    }
    lines.add(currentLine);
}

    private boolean firstCharacter = false;
    private TextMesh createQuadVertices(GLText text,
                                        List<TextLine> lines)
    {
        text.setNumberOfLines(lines.size());
        double cursorX = 0.5;
        double cursorY = 0.5;
        firstCharacter = true;
        totalHeight = 0.0f;

        List<Float> vertices = new ArrayList<Float>();
        List<Float> textureCoords = new ArrayList<Float>();
        for(int i = 0; i < lines.size(); i++) {
            maxCharOffset = 0.0f;
            if(text.isCentered())
            {
                cursorX = 0.5 + (lines.get(i).getMaxLength() - lines.get(i).getLineLength()) / 2f;
            }

            for(Word word : lines.get(i).getWords())
            {
                for(GLCharacter letter : word.getCharacters())
                {
                    addVerticesForCharacter(cursorX, cursorY, text.getFontSize(), letter, vertices);
                    addTexCoords(textureCoords, letter.getxTextureCoord(),
                            letter.getyTextureCoord(), letter.getxMaxTextureCoord(),
                            letter.getyMaxTextureCoord());
                    cursorX += letter.getxAdvance() * text.getFontSize();
                }
                cursorX += metaData.getSpaceWidth() * text.getFontSize();
            }

            cursorX = 0.5;
            cursorY += LINE_HEIGHT * text.getFontSize();
        }

        // Determine the height of the text area
        for(int i = 0; i < lines.size(); i++)
        {
            totalHeight += (LINE_HEIGHT * text.getFontSize());
        }
        totalHeight -= maxCharOffset;

        return new TextMesh(listToArray(vertices),
                listToArray(textureCoords), maxX - minX, totalHeight);
    }
    private void addVerticesForCharacter(double cursorX, double cursorY,
                                         float fontSize,
                                         GLCharacter character,
                                         List<Float> vertices)
    {
        double x = cursorX + (character.getxOffset() * fontSize);
        double y = cursorY + (character.getyOffset() * fontSize);
        maxCharOffset = Math.max(maxCharOffset, (float)(character.getyOffset()));
        System.out.println("Offset: " + (character.getyOffset() * fontSize));
        double properX = (2 * x) - 1;
        double properY = (-2 * y) + 1;
        double properMaxX = (2 * (x + (character.getSizeX() * fontSize))) - 1;
        double properMaxY = (-2 * (y + (character.getSizeY() * fontSize))) + 1;

        if(firstCharacter)
        {
            minX = (float)properX;
            maxX = (float)properMaxX;
            firstCharacter = false;
        }

        minX = ((float)properX < minX) ? (float)properX : minX;
        maxX = ((float)properMaxX > maxX) ? (float)properMaxX : maxX;
        addVertices(vertices, properX, properY, properMaxX, properMaxY);
    }

    private static void addVertices(List<Float> vertices, double x, double y,
                                    double maxX, double maxY)
    {
        // Triangle 1
        vertices.add((float) x);        vertices.add((float) y);
        vertices.add((float) x);        vertices.add((float) maxY);
        vertices.add((float) maxX);     vertices.add((float) y);

        // Triangle 2
        vertices.add((float) maxX);     vertices.add((float) y);
        vertices.add((float) x);        vertices.add((float) maxY);
        vertices.add((float) maxX);     vertices.add((float) maxY);
    }

    private static void addTexCoords(List<Float> texCoords, double x, double y,
                                     double maxX, double maxY)
    {
        // Triangle 1
        texCoords.add((float) x);       texCoords.add((float) y);
        texCoords.add((float) x);       texCoords.add((float) maxY);
        texCoords.add((float) maxX);    texCoords.add((float) y);

        // Triangle 2
        texCoords.add((float) maxX);    texCoords.add((float) y);
        texCoords.add((float) x);       texCoords.add((float) maxY);
        texCoords.add((float) maxX);    texCoords.add((float) maxY);
    }

    private static float[] listToArray(List<Float> listOfFloats)
    {
        float[] array = new float[listOfFloats.size()];
        for(int i = 0; i < array.length; i++)
        {
            array[i] = listOfFloats.get(i);
        }
        return array;
    }
}
