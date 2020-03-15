package org.firstinspires.ftc.teamcode.Autonomous;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;

import com.vuforia.Image;
import com.vuforia.PIXEL_FORMAT;
import com.vuforia.Vuforia;

import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.teamcode.DebugMSG;
import org.firstinspires.ftc.teamcode.robot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.os.SystemClock.sleep;
import static edu.spa.ftclib.internal.Global.telemetry;

public class SkystoneDetectorNEW {
    VuforiaLocalizer vuforia;

    // We initialize the detector
    public SkystoneDetectorNEW(VuforiaLocalizer vuforia)
    {
        this.vuforia = vuforia;
    }

    // Some enums to know where the skystone is
    public enum skystonePos
    {
        LEFT, CENTER, RIGHT, NONE
    }

    // Will return where the skystone is
    public skystonePos vuforiaScan(boolean save, boolean red)
    {
        Image isRgb = null;
        double YellowCountLeft = 1;
        double YellowCountCenter = 1;
        double YellowCountRight = 1;
        double BlackCountLeft = 1;
        double BlackCountCenter = 1;
        double BlackCountRight = 1;

        long greenRight = 0;
        long greenCenter = 0;
        long greenLeft = 0;

        skystonePos pos = skystonePos.NONE;

        Vuforia.setFrameFormat(PIXEL_FORMAT.RGB565, true); // Let us to return an image in RGB
        VuforiaLocalizer.CloseableFrame closeableFrame = null;
        this.vuforia.setFrameQueueCapacity(1);
        while (isRgb == null)
        {
            try
            {
                closeableFrame = this.vuforia.getFrameQueue().take(); // We take the picture with vuforia
                long nImages = closeableFrame.getNumImages(); // Get how many pictures did vuforia take

                for (int i=0;i<nImages;i++)
                {
                    if (closeableFrame.getImage(i).getFormat() == PIXEL_FORMAT.RGB565) // If the image has color
                    {
                        isRgb = closeableFrame.getImage(i);
                        break;
                    }
                }
            }
            catch (InterruptedException exc)
            {
            }
            finally {
                if (closeableFrame != null) closeableFrame.close();
            }
        }

        // We need to create a bitmap


        if (isRgb!= null)
        {
            Bitmap bitmap = Bitmap.createBitmap(isRgb.getWidth(), isRgb.getHeight(), Bitmap.Config.RGB_565); // We create a single bitmap without anything inside
            bitmap.copyPixelsFromBuffer(isRgb.getPixels()); //We transfer the image to the bitmap

            // Debugging Process

            String path = Environment.getExternalStorageDirectory().toString(); // Path to save the image
            FileOutputStream out = null;

            String bitmapName;
            String bitmapNameCropped;

            if (red) {
                bitmapName = "Red.png";
                bitmapNameCropped = "CropRed.png";
            }
            else
            {
                bitmapName = "Blue.png";
                bitmapNameCropped = "CropBlue.png";
            }
            // Just to debug, it saves the bitmap in the phone
            if (save)
            {
                try {
                    File file = new File(path, bitmapName);
                    out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                } catch (Exception e){
                    e.printStackTrace();
                } finally {
                    try
                    {
                        if (out != null)
                        {
                            out.flush();
                            out.close();
                        }
                    }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
            }

            int cropStartX = (int) (bitmap.getWidth() / 1.84);
            int cropEndX = (int) (bitmap.getWidth() / 1.67);
            int xWidth = cropEndX - cropStartX;

            int cropStartY = 0;
            int yHeight = bitmap.getHeight();

            bitmap = Bitmap.createBitmap(bitmap, 853, cropStartY, 991-853, yHeight);

            if (save)
            {
                try {
                    File file = new File(path, bitmapNameCropped);
                    out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                } catch (Exception e){
                    e.printStackTrace();
                } finally {
                    try
                    {
                        if (out != null)
                        {
                            out.flush();
                            out.close();
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            // Y dimensions



            bitmap = Bitmap.createScaledBitmap(bitmap, 71/3, 720/3, true);

            if (save)
            {
                try {
                    File file = new File(path, "Scaled.png");
                    out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                } catch (Exception e){
                    e.printStackTrace();
                } finally {
                    try
                    {
                        if (out != null)
                        {
                            out.flush();
                            out.close();
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }


            int pixel;

            int rightStoneStart = 0;
            int centerStoneStart = bitmap.getHeight() / 3;
            int leftStoneStart = bitmap.getHeight() / 3 * 2;

            int blockLength = bitmap.getWidth() - 1;

            // We scan the right block first

            for (int i=0; i < blockLength; i++)
            {
                for (int j = rightStoneStart; j < centerStoneStart; j ++)
                {
                    pixel = bitmap.getPixel(i,j);
                    if (Color.red(pixel) < 200 || Color.blue(pixel) <200 || Color.green(pixel) < 200)
                    {
                        if (Color.red(pixel) < 100 && Color.blue(pixel) < 100)
                        {
                            BlackCountRight += 1;
                        }
                        else
                        {
                            YellowCountRight += 1;
                        }
                    }
                }
            }

            for (int i=0; i < blockLength; i ++)
            {
                for (int j = centerStoneStart; j < leftStoneStart; j ++)
                {
                    pixel = bitmap.getPixel(i, j);
                    if (Color.red(pixel) < 200 || Color.blue(pixel) <200 || Color.green(pixel) < 200)
                    {
                        if (Color.red(pixel) < 100 && Color.blue(pixel) < 100)
                        {
                            BlackCountCenter += 1;
                        }
                        else
                        {
                            YellowCountCenter += 1;
                        }
                    }
                }
            }

            for (int i=0; i < blockLength; i ++)
            {
                for (int j = leftStoneStart; j < bitmap.getHeight(); j ++)
                {
                    pixel = bitmap.getPixel(i,j);
                    if (Color.red(pixel)<  200 || Color.blue(pixel) <200 || Color.green(pixel) < 200)
                    {
                        if (Color.red(pixel) < 100 && Color.blue(pixel) < 100)
                        {
                            BlackCountLeft += 1;
                        }
                        else
                        {
                            YellowCountLeft += 1;
                        }
                    }
                }
            }

            double leftRatio = BlackCountLeft / YellowCountLeft;
            double centerRatio = BlackCountCenter / YellowCountCenter;
            double rightRatio = BlackCountRight / YellowCountRight;

            // The block with the highest black/yellow ratio is the skystone
            double max = Math.max(rightRatio, Math.max(leftRatio, centerRatio));

            DebugMSG.msg(""+max);



            DebugMSG.msg("Left " + BlackCountLeft + "/" + YellowCountLeft + " = " + BlackCountLeft / YellowCountLeft);
            DebugMSG.msg("Center " + BlackCountCenter + "/" + YellowCountCenter + " = " + BlackCountCenter / YellowCountCenter);
            DebugMSG.msg("Right" + BlackCountRight + "/" + YellowCountRight + " = " + BlackCountRight / YellowCountRight);

            if (max == centerRatio)
            {
                pos = skystonePos.CENTER;
                DebugMSG.msg("CENTER");
                return pos;
            }
            else if (max == leftRatio)
            {
                pos = skystonePos.LEFT;
                DebugMSG.msg("LEFT");
                return pos;
            }
            else if (max == rightRatio)
            {
                pos = skystonePos.RIGHT;
                DebugMSG.msg("RIGHT");
                return pos;
            }



            /*
            if (leftRatio > centerRatio && leftRatio > rightRatio)
            {
                pos = skystonePos.LEFT;
            }
            else if (centerRatio > leftRatio && centerRatio > rightRatio)
            {
                pos = skystonePos.CENTER;
            }
            else if (rightRatio > centerRatio && rightRatio > leftRatio)
            {
                pos = skystonePos.RIGHT;
            }

             */
        }
        return pos;
    }
}
