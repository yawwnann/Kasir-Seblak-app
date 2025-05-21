// Tambahkan di file TambahMenuActivity.kt (di luar kelas) atau file utilitas
// Pastikan import java.io.File, java.io.FileOutputStream, java.io.InputStream, android.util.Log sudah ada
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.content.Context
import android.net.Uri
import android.util.Log

fun saveImageToInternalStorage(context: Context, originalUri: Uri, baseFileName: String): String? {
    try {
        val inputStream = context.contentResolver.openInputStream(originalUri) ?: return null

        val sanitizedBaseName = baseFileName.replace(Regex("[^a-zA-Z0-9.-]"), "_").take(50)
        val fileName = "menu_img_${sanitizedBaseName}_${System.currentTimeMillis()}.jpg"

        // Simpan di direktori internal aplikasi (filesDir)
        val file = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(file)

        inputStream.copyTo(outputStream) // Salin data gambar

        inputStream.close()
        outputStream.close()

        Log.d("ImageSave", "Image saved to: ${file.absolutePath}")
        return file.absolutePath // Kembalikan path absolut dari file yang baru disimpan
    } catch (e: IOException) {
        Log.e("ImageSave", "Error saving image to internal storage", e)
        return null
    } catch (e: Exception) {
        Log.e("ImageSave", "An unexpected error occurred", e)
        return null
    }
}