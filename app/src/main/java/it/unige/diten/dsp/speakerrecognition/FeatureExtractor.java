package it.unige.diten.dsp.speakerrecognition;

// TODO FeatureExtractor: intent.

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.util.Log;

import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.regex.Pattern;

public class FeatureExtractor extends AsyncTask <String, Integer, Boolean> {

    public final static int MFCC_COUNT = 13;
    public final static String TAG = "FeatureExtractor";

    private static ProgressDialog cProgressRecorder;
    private static Context cContext;

    public static double[][] MFCC = null;
    public static double[][] DeltaDelta = null;

    protected void onPreExecute()
    {
        cContext = MainActivity.context;

        cProgressRecorder = new ProgressDialog(cContext);
        cProgressRecorder.setProgressNumberFormat(null);
        cProgressRecorder.setMax(100);

        cProgressRecorder = ProgressDialog.show(cContext, "Extracting features...", "just deal with it.");
    }

    @Override
    protected Boolean doInBackground(String... params)
    {
        try {
            // params[0] = name of the audio file
            Log.i(TAG,"Feature extraction started");
            //MFCC = extractMFCC(params[0]);
            MFCC = extractMFCC(MainActivity.PATH + "/" + "test.wav");
            Log.i(TAG,"Feature extraction ended.");
            DeltaDelta = DD.computeDD_0(MFCC, 2); // 2 is precision


            // If recognition mode is on:
            if (!MainActivity.isTraining)
            {
                // Delete wav file
                File file = new File(params[0]);
                file.delete();
                // Send SVM intent so that SVM recognition may start
                Intent intent = new Intent("it.unige.diten.dsp.speakerrecognition.SVM_EXTRACT");
                cContext.sendBroadcast(intent);
            }
            else
            // if training mode is on:
            {
                /**
                 * Save to file
                 * save with the same filename of the .wav with a different extension:
                 * ff = feature file
                 */
                String outputFileName = params[0].replace(MainActivity.AUDIO_EXT, MainActivity.FEATURE_EXT);
                writeFeatureFile(outputFileName, MFCC, DeltaDelta);
            }
        }
        catch(Exception e)
        {
            Log.e(TAG, e.getMessage());
            return(Boolean.FALSE);
        }

        return(Boolean.TRUE);
    }

    @Override
    protected void onProgressUpdate(Integer... progress)
    {
        // TODO: this is never called. Make this statement false.
        cProgressRecorder.setMessage(progress[0] + "%");
    }

    @Override
    protected void onPostExecute(Boolean cv)
    {
        cProgressRecorder.dismiss();
    }


    public static void writeFeatureFile(String fileName, double[][] MFCC, double[][] DeltaDelta)
    {
        // Stampo su file testuale i vettori
        try
        {
            FileWriter writer = new FileWriter(fileName, true);
            /*
            * output:
            * %MFCC                             %DD
            * MFCC0     MFCC1   MFCC2   ...     DD0     DD1
            * frame1
            * frame2
            * .
            * .
            * .
            */
            for(int f = 0; f < Framer.getFrames().length; f++)
            {
                String line = "";

                for (int k = 0; k < MFCC_COUNT; k++)
                    line += Double.toString(MFCC[f][k]) + "\t";


                for (int k = 0; k < DD.DD_COUNT; k++)
                    line += Double.toString(DeltaDelta[f][k]) + "\t";

                line = line.substring(0, line.length() - 2) + "\r\n";

                writer.write(line);
            }
            writer.close();
        }
        catch (IOException e)
        {
            Log.e(TAG, e.getMessage());
        }
    }

    public static double [][] extractMFCC(String audioFilename) throws java.lang.Exception
    {
        Framer.readFromFile(audioFilename);
        final Frame[] frames = Framer.getFrames();

        double[][] mfcc = new double[frames.length][]; // No need to create rows (FEThread already does it)

        int numCores = MainActivity.numCores;

        Runnable[] runnables = new FEThread[numCores];
        Thread[] threads = new Thread[numCores];
        for(int C = 0; C < numCores; C++) {
            runnables[C] = new FEThread(C, numCores, frames, mfcc);
            threads[C] = new Thread(runnables[C]);
            threads[C].start();
        }
        for(int C = 0; C < numCores; C++)
            threads[C].join();

        return mfcc;
    }
}
