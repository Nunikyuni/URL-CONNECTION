package clases;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JProgressBar;
import javax.swing.table.AbstractTableModel;

public class DownloadsTableModel extends AbstractTableModel implements Observer {
    // Membuat nama kolom dengan array
    private static final String[] columnNames = {"URL", "Size", "Progress", "Status"};
    // Untuk menyesuakian kolom, kita definisakan kelas array
    private static final Class[] columnClasses = {String.class,
        String.class, JProgressBar.class, String.class};
    // Array list untuk mengunduh objek
    private ArrayList<Download> downloadList = new ArrayList<Download>();

    public void addDownload(Download download) {
        download.addObserver(this);
        // menambahkan fungsi objek untuk daftar list unduhan
        downloadList.add(download);
        fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
    }

    public Download getDownload(int row) {
        return downloadList.get(row);
    }

    public void clearDownload(int row) {
        downloadList.remove(row);
        fireTableRowsDeleted(row, row);
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }


    @Override
    public Class getColumnClass(int col) {
        return columnClasses[col];
    }

    @Override
    public int getRowCount() {
        return downloadList.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        Download download = (Download) downloadList.get(row);
        switch (col) {
            case 0: // URL
                return download.getUrl();
            case 1: // Ukuran
                int size = download.getSize();
                return (size == -1) ? "" : Integer.toString(size);
            case 2: // Progress 
                return new Float(download.getProgress());
            case 3: // Status
                return Download.STATUSES[download.getStatus()];
        }
        return "";
    }

    //method memberi perubahan apabila menerima pemberitahuan dari kelas objek unduh
    @Override
    public void update(Observable o, Object arg) {
        int index = downloadList.indexOf(o);
        fireTableRowsUpdated(index, index);
    }
}
