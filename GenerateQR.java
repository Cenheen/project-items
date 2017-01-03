/**生成二维码 **/
public class MatrixToImageWriter {

    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MARGIN = 1;
    private static final String FORMAT = "png";

    private MatrixToImageWriter() {}

    public static void createRqCode(String textOrUrl, int width, int height, OutputStream toStream) throws WriterException, IOException {
        Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
//        hints.put(EncodeHintType.CHARACTER_SET, "utf-8"); // 内容所使用字符集编码
//        hints.put(EncodeHintType.MARGIN, new Integer(MARGIN));

        BitMatrix bitMatrix = new MultiFormatWriter().encode(textOrUrl, BarcodeFormat.QR_CODE, width, height, hints);
        BufferedImage image = toBufferedImage(bitMatrix);
        writeToStream(image, FORMAT, toStream);
    }

    private static BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
            }
        }
        return image;
    }

    public static void writeToStream(BufferedImage image, String format, OutputStream stream) throws IOException {
        if (!ImageIO.write(image, format, stream)) {
            throw new IOException("Could not write an image of format " + format);
        }
    }
}

JSP中写
<img src="<%request.getContextPath();%>/test/genQrCode?url=123&width=250&height=250">
Controller中：
@RequestMapping("/genQrCode")
public void genQrCode(String url, HttpServletResponse response, Integer width, Integer height) {
    try {
        int iWidth = (width == null ? 200 : width);
        int iHeight = (height == null ? 200 : height);

        MatrixToImageWriter.createRqCode(url, iWidth, iHeight, response.getOutputStream());
    } catch (Exception e) {
        LOGGER.error("生成二维码失败 url:", url, ", e:", e);
    }
}
基本流程就是JSP中img响应到Controller中方法
