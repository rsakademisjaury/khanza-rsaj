package laporan;

/**
 *
 * @author Mustafa Daeng Muma
 */
public interface TukarPrioritasListener {
    void onSiapTukar(String noRawat1, String kdPenyakit1, String prioritas1,
                     String noRawat2, String kdPenyakit2, String prioritas2);
}