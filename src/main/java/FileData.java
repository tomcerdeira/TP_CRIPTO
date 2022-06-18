public class FileData {
    byte[] data;
    int size;
    String ext;

    public FileData(byte[] data, int size) {
        this.data = data;
        this.size = size;
        this.ext = null;
    }

    public byte[] getData() {
        return data;
    }

    public int getSize() {
        return size;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getExt() {
        return ext;
    }
}
