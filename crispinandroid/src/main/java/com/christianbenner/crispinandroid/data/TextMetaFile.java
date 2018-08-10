package com.christianbenner.crispinandroid.data;

import android.content.Context;
import android.util.DisplayMetrics;

import com.christianbenner.crispinandroid.util.TextMeshCreator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Christian Benner on 27/11/2017.
 */

public class TextMetaFile
{
    private Context context;

    private static final int PAD_TOP = 0;
    private static final int PAD_LEFT = 1;
    private static final int PAD_BOTTOM = 2;
    private static final int PAD_RIGHT = 3;

    private static final int DESIRED_PADDING = 3;
    private int padding[];
    private int paddingWidth;
    private int paddingHeight;
    private int imageWidth;

    private double verticalPerPixelSize;
    private double horizontalPerPixelSize;
    private double aspectRatio;
    private double spaceWidth;

    // Store the data on the character
    private Map<Integer, GLCharacter> metaData = new HashMap<Integer, GLCharacter>();

    private static final String SPLITTER = " ";
    private static final String NUMBER_SEPERATOR = ",";

    private Map<String, String> values = new HashMap<String, String>();

    private BufferedReader reader;

    public TextMetaFile(Context context, int resourceId)
    {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        aspectRatio = (double)metrics.widthPixels / (double)metrics.heightPixels;
        this.context = context;
        openFile(resourceId);
        loadPaddingData();
        loadLineSizes();
        this.imageWidth = getValueOfVariable("scaleW");
        loadCharacterData();
        close();
    }

    private void openFile(int resourceId)
    {
        InputStream is = context.getResources().openRawResource(resourceId);
        reader = new BufferedReader(new InputStreamReader(is));
    }

    private void close()
    {
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCharacterData()
    {
        processNextLine();
        processNextLine();
        while(processNextLine())
        {
            GLCharacter c = loadCharacter();
            if(c != null)
            {
                metaData.put(c.getID(), c);
            }
        }
    }

    private GLCharacter loadCharacter()
    {
        int id = getValueOfVariable("id");
        if (id == TextMeshCreator.SPACE_ASCII) {
            this.spaceWidth = (getValueOfVariable("xadvance") - paddingWidth) *
                    horizontalPerPixelSize;
            return null;
        }
        double xTex = ((double) getValueOfVariable("x") + (padding[PAD_LEFT] - DESIRED_PADDING))
                / imageWidth;
        double yTex = ((double) getValueOfVariable("y") + (padding[PAD_TOP] - DESIRED_PADDING))
                / imageWidth;
        int width = getValueOfVariable("width") - (paddingWidth - (2 * DESIRED_PADDING));
        int height = getValueOfVariable("height") - ((paddingHeight) - (2 * DESIRED_PADDING));
        double quadWidth = width * horizontalPerPixelSize;
        double quadHeight = height * verticalPerPixelSize;
        double xTexSize = (double) width / imageWidth;
        double yTexSize = (double) height / imageWidth;
        double xOff = (getValueOfVariable("xoffset") + padding[PAD_LEFT] - DESIRED_PADDING) *
                horizontalPerPixelSize;
        double yOff = (getValueOfVariable("yoffset") + (padding[PAD_TOP] - DESIRED_PADDING)) *
                verticalPerPixelSize;
        double xAdvance = (getValueOfVariable("xadvance") - paddingWidth) * horizontalPerPixelSize;
        return new GLCharacter(id, xTex, yTex, xTexSize, yTexSize, xOff, yOff, quadWidth,
                quadHeight, xAdvance);
    }

    private void loadPaddingData()
    {
        processNextLine();
        this.padding = getValuesOfVariable("padding");
        this.paddingWidth = padding[PAD_LEFT] +
                padding[PAD_RIGHT];
        this.paddingHeight = padding[PAD_TOP] +
                padding[PAD_BOTTOM];
    }

    private void loadLineSizes()
    {
        processNextLine();
        int lineHeightPixels =
                getValueOfVariable("lineHeight") - paddingHeight;
        verticalPerPixelSize = TextMeshCreator.LINE_HEIGHT /
                (double) lineHeightPixels;
        horizontalPerPixelSize = verticalPerPixelSize /
                aspectRatio;
    }

    private int getValueOfVariable(String variable)
    {
        return Integer.parseInt(values.get(variable));
    }

    private int[] getValuesOfVariable(String variable)
    {
        String[] numbers = values.get(variable).split(NUMBER_SEPERATOR);
        int[] actualValues = new int[numbers.length];
        for(int i = 0; i < actualValues.length; i++)
        {
            actualValues[i] = Integer.parseInt(numbers[i]);
        }
        return actualValues;
    }

    private boolean processNextLine()
    {
        values.clear();
        String line = null;
        try
        {
            line = reader.readLine();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        if(line == null)
        {
            return false;
        }

        for(String part : line.split(SPLITTER))
        {
            String[] valuePairs = part.split("=");
            if(valuePairs.length == 2)
            {
                values.put(valuePairs[0], valuePairs[1]);
            }
        }
        return true;
    }

    public void printData()
    {
        System.out.println("MetaFile: " + "Padding[" + padding[0] + ", " + padding[1] + ", " +
                padding[2] + ", " + padding[3] + "], " +
                "Aspect Ratio[" + aspectRatio + "]");

        for(GLCharacter c : metaData.values())
        {
            System.out.print("MetaFile: ");
            c.printValues();
        }
    }

    public double getSpaceWidth()
    {
        return spaceWidth;
    }

    public GLCharacter getCharacter(int ascii)
    {
        return metaData.get(ascii);
    }
}
