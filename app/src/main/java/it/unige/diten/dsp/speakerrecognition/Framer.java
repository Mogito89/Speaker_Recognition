package it.unige.diten.dsp.speakerrecognition;

import it.unige.diten.dsp.speakerrecognition.WavIO.WavIO;

/**
 * Framer
 * E' una classe astratta che si occupa di leggere un file .WAV per poi suddividerlo in frames
 * secondo i parametri noti (public final static...)
 * L'unico parametro che NON VA CAMBIATO � BPS in quanto abbiamo utilizzato il tipo short per lo
 * storage e l'elaborazione dei campioni, e dunque BPS deve essere 2.
 */
public abstract class Framer
{
    /// Duration of the single frame in ms
    public final static int FRAME_LENGTH_MS = 32;
    /// Expected sample rate in Hz
    public final static int SAMPLE_RATE = 8000;
    /// Number of samples in a single frame
    public final static int SAMPLES_IN_FRAME = SAMPLE_RATE * FRAME_LENGTH_MS / 1000;
    /// Frame overlap factor
    public final static float FRAME_OVERLAP_FACTOR = .75f;
    /// Number of Byte(s) per sample
    public final static int BPS = 2;
    /// Frame size in Bytes
    public final static int FRAME_BYTE_SIZE = SAMPLES_IN_FRAME * BPS;
    /// Distance in Bytes between the beginning of a frame and the following
    public final static int FRAME_BYTE_SPACING = (int)((float)FRAME_BYTE_SIZE * (1.0f-FRAME_OVERLAP_FACTOR));

    /// Container for all frames
    private static Frame[] frames = null;

    /// Convert byte[] to short[] with zero-filling
    private static short[] toShortArray(byte[] src, int pos_byte, int len)
    {
        short[] retV = new short[len];

        for (int i = 0; i < len * 2; i += 2)
        {
            // Zero-filling
            if (pos_byte + i + 1 >= src.length)
                retV[i / 2] = 0;
            else {                                      // Copy two bytes into one short
                retV[i / 2] =   (short) ((src[pos_byte + i])            & 0x00FF); //LSB
                retV[i / 2] +=  (short) ((src[pos_byte + i + 1] << 8)   & 0xFF00); //MSB
            }
        }

        return (retV);
    }

    /// Read WAVE file from SDCard
    public static void readFromFile(String fileName) throws Exception
    {
        WavIO readWAV = new WavIO(fileName);
        readWAV.read();

        if (readWAV.GetSampleRate() != SAMPLE_RATE)
            throw new Exception("Framer::readFromWAV: Invalid sample rate!");
        // Clear old data (garbage collector)
        frames = null;

        int frameCount = readWAV.myData.length / FRAME_BYTE_SPACING;
        frames = new Frame[frameCount];

        for (int i = 0; i < frameCount; i++)
            frames[i].data = toShortArray(readWAV.myData, i * FRAME_BYTE_SPACING, SAMPLES_IN_FRAME);
    }

    /// Returns frame array
    public static Frame[] getFrames()
    {
        return (frames);
    }
}