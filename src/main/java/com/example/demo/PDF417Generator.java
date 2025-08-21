package com.example.demo;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.pdf417.PDF417Writer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

public class PDF417Generator {
    public static void generatePDF417(String data, String outputPath) throws IOException, WriterException {
        PDF417Writer pdf417Writer = new PDF417Writer();

        Map<EncodeHintType, ?> hints = Map.of(
                EncodeHintType.CHARACTER_SET, "ISO-8859-1",
                EncodeHintType.ERROR_CORRECTION, 5,
                EncodeHintType.MARGIN, 10
//                EncodeHintType.PDF417_DIMENSIONS, new Dimensions(1, 30, 1, 17)
        );

        BitMatrix matrix = pdf417Writer.encode(data, BarcodeFormat.PDF_417, 1200, 300, hints);
        MatrixToImageWriter.writeToPath(matrix, "PNG", Paths.get("pdf417.png"));
    }

    public static void main(String[] args) throws IOException, WriterException {

        char RS = 30;
        char GS = 29;
        char FS = 28;
        char EOT = 4;

        String data = new StringBuilder()
                .append("[)>").append(RS).append("01").append(GS)
                .append("0243201").append(GS) // 主运单号
                .append("840").append(GS) // 服务类型（国际快递）
                .append("804").append(GS) // 包裹序列号
                .append("288379593956").append(GS) // 包裹跟踪号（12位
                .append("FDEG").append(GS) // 发件人国家代码
                .append("205025670").append(GS) // 客户账号
                .append("127").append(GS).append(GS) // 包裹类型
                .append("1/1").append(GS) // 包裹序号
                .append("1.00LB").append(GS) // 重量
                .append("N").append(GS) // 重量单位
                .append("85 E 8th Ave").append(GS) // 地址
                .append("Columbus").append(GS) // 城市
                .append("OH").append(GS) // 州代码
                .append("Will C").append(RS) // 收件人姓名
                .append("06").append(GS) // 数据子集标识
                .append("10ZGH007").append(GS) // 清关代码
                .append("12Z3345241123").append(GS) // 海关申报号
                .append("20Z").append(FS).append(GS) // 填充字段
                .append("31Z9632080400205025670500288379593956").append(GS) // 原始跟踪号
                .append("9KOrder #80882231214").append(GS).append(RS).append(EOT) // 订单号

                .toString();


        generatePDF417(data, "output.png"); // 输出到PNG文件，可以调整为其他格式如SVG等
    }
}
