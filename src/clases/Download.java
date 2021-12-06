package clases;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Observable;
//import javax.swing.JOptionPane;



public class Download extends Observable implements Runnable {
    // Ukuran buffer unduhan maksimum 
    private static final int MAX_BUFFER_SIZE = 1024;
    // inisialisasi status unduhan
    public static final String STATUSES[] = {"Downloading", "Paused", "Complete", "Cancelled", "Error"};
    // inisialisai kode status
    public static final int DOWNLOADING = 0;
    public static final int PAUSED = 1;
    public static final int COMPLETE = 2;
    public static final int CANCELLED = 3;
    public static final int ERROR = 4;
    private final URL url; // download URL
    private int size; // ukuran
    private int downloaded; // bytes yang diunduh
    private int status; // status unduhan saat ini
    private final String folder; // Direktori tujuan disimpan file yang di unduh

   
    //Constructor
   
    public Download(URL url, String folder) {
        this.url = url;
        size = -1;
        downloaded = 0;
        status = DOWNLOADING;
        this.folder = folder;
        download(); 
    }

   
    public String getUrl() {
        return url.toString();
    }

 
    public int getSize() {
        return size;
    }

    public float getProgress() {
        return ((float) downloaded / size) * 100;
    }

   
    public int getStatus() {
        return status;
    }

  
    public void pause() {
        status = PAUSED;
        stateChanged();
    }


    public void resume() {
        status = DOWNLOADING;
        stateChanged();
        download();
    }

    public void cancel() {
        status = CANCELLED;
        stateChanged();
    }

    private void error() {
        status = ERROR;
        stateChanged();
    }


    private void download() {
        Thread thread = new Thread(this);
        thread.start();
    }

    private String getFileName(URL url) {
        String fileName = url.getFile();
        return fileName.substring(fileName.lastIndexOf('/') + 1);
    }

    public void run() {
        RandomAccessFile file = null;
        InputStream stream = null;

        try {
            // membuka koneksi HTTP di URL
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            // menunjukkan dari file yang akan diunduh
            connection.setRequestProperty("Range", "bytes=" + downloaded + "-");

            // menghubungkan ke server
            connection.connect();

            // Verifikasi bahwa kode respons HTTP berada dalam kisaran 200
            if (connection.getResponseCode() / 100 != 2) {
                error();
            }

            // Memeriksa apakah ukuran konten valid
            int contentLength = connection.getContentLength();
            if (contentLength < 1) {
                error();
            }

       
            if (size == -1) {
                size = contentLength;
                stateChanged();
            }

            file = new RandomAccessFile(this.folder + getFileName(url), "rw");
            file.seek(downloaded);
            /**
              * mendpatkan aliran data (stream) dari koneksi terbuka.
              */
            stream = connection.getInputStream();
            while (status == DOWNLOADING) {
               /**
                  * Menentukan ukuran buffer tergantung pada seberapa banyak
                  * file tetap harus diunduh.
                  */
                byte buffer[];
                if (size - downloaded > MAX_BUFFER_SIZE) {
                    buffer = new byte[MAX_BUFFER_SIZE];
                } else {
                    buffer = new byte[size - downloaded];
                }
                // Membaca byte dari server dan buffer 
                // menetapkan variabel baca jumlah byte yang benar-benar dibaca.
                int read = stream.read(buffer);
               /**
                  * Jika jumlah byte yang dibaca adalah -1 itu berarti unduhan
                  * selesai
                  */
                if (read == -1) {
                    break;
                }

                // inisialisasi buffer di atas file
                file.write(buffer, 0, read);
                downloaded += read;
                stateChanged();
            }
           /**
              * Ubah status menjadi Complete. Pada saat ini
              * unduhan telah selesai sepenuhnya.
              */
            if (status == DOWNLOADING) {
                status = COMPLETE;
                stateChanged();
                
                
            }
        } catch (Exception e) {
            error();
        } finally {
            // Cierra el archivo
            if (file != null) {
                try {
                    file.close();
                } catch (Exception e) {
                }
            }

            
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {
                }
            }
        }
    }

  /**
      * notifikasi bahwa status unduhan telah berubah
      */
    private void stateChanged() {
        setChanged();
        notifyObservers();
    }
}
