package it.unige.diten.dsp.speakerrecognition;
/*
 * FFT
 * Questa classe per il calcolo della FFT � stata presa da internet
 * e lievemente modificata, tuttavia abbiamo deciso di non usarla per i seguenti motivi:
 * -    Implementare la DFT � facile e permette di arrivare velocemente ad una release funzionante.
 * -    E' comunque auspicabile scrivere la FFT a mano per scopi didattici, ma essendo time
 *      consuming conviene prendersi del tempo per farlo in futuro in modo da sostituirla alla DFT.
 */
public class FFT {

    int n, m;

    // Lookup tables. Only need to recompute when size of FFT changes.
    double[] cos;
    double[] sin;

    public FFT(int n) {
        this.n = n;
        this.m = (int)  (Math.log(n) / Math.log(2));

        // Make sure n is a power of 2
        if (n != (1 << m))
            throw new RuntimeException("FFT length must be power of 2");

        // precompute tables
        cos = new double[n / 2];
        sin = new double[n / 2];

        for (int i = 0; i < n / 2; i++) {
            cos[i] = Math.cos(-2 * Math.PI * i / n);
            sin[i] = Math.sin(-2 * Math.PI * i / n);
        }

    }

    /**
     * @brief Everybody do the fft.
     * @usage fft (src, dest)
     * @param x
     * @param y
     */
    public void fft(short[] x, short[] y) {
        int i, j, k, n1, n2, a;
        double c, s, t1, t2;

        // Bit-reverse
        j = 0;
        n2 = n / 2;
        for (i = 1; i < n - 1; i++) {
            n1 = n2;
            while (j >= n1) {
                j = j - n1;
                n1 = n1 / 2;
            }
            j = j + n1;

            if (i < j) {
                t1 = x[i];
                x[i] = x[j];
                x[j] = (short)  t1;
                t1 = y[i];
                y[i] =          y[j];
                y[j] = (short)  t1;
            }
        }

        // FFT
        n1 = 0;
        n2 = 1;

        for (i = 0; i < m; i++) {
            n1 = n2;
            n2 = n2 + n2;
            a = 0;

            for (j = 0; j < n1; j++) {
                c = cos[a];
                s = sin[a];
                a += 1 << (m - i - 1);

                for (k = j; k < n; k = k + n2) {
                    t1 = c * x[k + n1] - s * y[k + n1];
                    t2 = s * x[k + n1] + c * y[k + n1];
                    x[k + n1] =     (short)(x[k] - t1);
                    y[k + n1] =     (short)(y[k] - t2);
                    x[k] =          (short)(x[k] + t1);
                    y[k] =          (short)(y[k] + t2);
                }
            }
        }
    }
}