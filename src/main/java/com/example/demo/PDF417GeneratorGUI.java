package com.example.demo;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.pdf417.PDF417Writer;
import com.google.zxing.pdf417.encoder.Dimensions;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.imageio.ImageIO;
import java.util.Base64;

public class PDF417GeneratorGUI extends JFrame {
    private JTextArea inputTextArea;
    private JLabel barcodeLabel;
    private JButton generateButton;
    private JButton copyButton;
    private JButton copyImageButton;
    private JButton saveAsButton;
    private JScrollPane inputScrollPane;
    private JScrollPane barcodeScrollPane;
    private BufferedImage currentBarcodeImage;
    private JSlider widthSlider;
    private JLabel widthLabel;
    private JSlider columnsSlider;
    private JLabel columnsLabel;
    private JSlider errorCorrectionSlider;
    private JLabel errorCorrectionLabel;
    private JButton uploadImageButton;
    private JTextArea decodeResultArea;
    private JScrollPane decodeScrollPane;
    private JSlider scaleSlider;
    private JLabel scaleLabel;
    private double currentScale = 1.0;
    private JCheckBox removeEndingCharsCheckBox;
    private JButton generateFromDecodeButton;
    
    // 控制字符常量
    private static final char RS = 30;  // Record Separator
    private static final char GS = 29;  // Group Separator  
    private static final char FS = 28;  // File Separator
    private static final char EOT = 4;  // End of Transmission

    public PDF417GeneratorGUI() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("PDF417 条码生成器 - 支持生成和解码");
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }

    private void initializeComponents() {
        // 输入文本区域
        inputTextArea = new JTextArea(15, 50);
        inputTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        inputTextArea.setLineWrap(true);
        inputTextArea.setWrapStyleWord(true);
        inputTextArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // 设置默认模板内容
        String defaultTemplate = getDefaultTemplate();
        inputTextArea.setText(defaultTemplate);
        
        inputScrollPane = new JScrollPane(inputTextArea);
        inputScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        inputScrollPane.setBorder(BorderFactory.createTitledBorder("输入内容 (可编辑模板)"));

        // 条码显示区域
        barcodeLabel = new JLabel();
        barcodeLabel.setHorizontalAlignment(JLabel.CENTER);
        barcodeLabel.setVerticalAlignment(JLabel.CENTER);
        barcodeLabel.setBackground(Color.WHITE);
        barcodeLabel.setOpaque(true);
        barcodeLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        barcodeScrollPane = new JScrollPane(barcodeLabel);
        barcodeScrollPane.setBorder(BorderFactory.createTitledBorder("生成的PDF417条码"));
        barcodeScrollPane.setPreferredSize(new java.awt.Dimension(500, 300));

        // 按钮
        generateButton = new JButton("生成PDF417条码");
        generateButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        generateButton.setBackground(new Color(70, 130, 180));
        generateButton.setForeground(Color.WHITE);
        generateButton.setFocusPainted(false);

        copyButton = new JButton("复制条码数据");
        copyButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        copyButton.setBackground(new Color(60, 179, 113));
        copyButton.setForeground(Color.WHITE);
        copyButton.setFocusPainted(false);
        copyButton.setEnabled(false);

        copyImageButton = new JButton("复制条码");
        copyImageButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        copyImageButton.setBackground(new Color(255, 140, 0));
        copyImageButton.setForeground(Color.WHITE);
        copyImageButton.setFocusPainted(false);
        copyImageButton.setEnabled(false);

        saveAsButton = new JButton("另存为");
        saveAsButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        saveAsButton.setBackground(new Color(138, 43, 226));
        saveAsButton.setForeground(Color.WHITE);
        saveAsButton.setFocusPainted(false);
        saveAsButton.setEnabled(false);

        // 宽度调整滑块
        widthSlider = new JSlider(800, 2400, 1200);
        widthSlider.setMajorTickSpacing(400);
        widthSlider.setMinorTickSpacing(100);
        widthSlider.setPaintTicks(true);
        widthSlider.setPaintLabels(true);
        widthSlider.setSnapToTicks(false);
        
        // 确保滑块值是4的整倍数
        widthSlider.addChangeListener(e -> {
            int value = widthSlider.getValue();
            int adjustedValue = (value / 4) * 4; // 确保是4的整倍数
            if (adjustedValue != value) {
                widthSlider.setValue(adjustedValue);
            }
            updateWidthLabel();
        });

        widthLabel = new JLabel("条码宽度: 1200px (高度: 300px)");
         widthLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
         widthLabel.setHorizontalAlignment(JLabel.CENTER);

        // 列数调整滑块
        columnsSlider = new JSlider(1, 30, 13);
        columnsSlider.setMajorTickSpacing(5);
        columnsSlider.setMinorTickSpacing(1);
        columnsSlider.setPaintTicks(true);
        columnsSlider.setPaintLabels(true);
        columnsSlider.setSnapToTicks(false);
        
        columnsSlider.addChangeListener(e -> updateColumnsLabel());

        columnsLabel = new JLabel("条码列数: 13列");
        columnsLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        columnsLabel.setHorizontalAlignment(JLabel.CENTER);

        // 错误纠正等级调整滑块
        errorCorrectionSlider = new JSlider(0, 8, 5);
        errorCorrectionSlider.setMajorTickSpacing(2);
        errorCorrectionSlider.setMinorTickSpacing(1);
        errorCorrectionSlider.setPaintTicks(true);
        errorCorrectionSlider.setPaintLabels(true);
        errorCorrectionSlider.setSnapToTicks(false);
        
        errorCorrectionSlider.addChangeListener(e -> updateErrorCorrectionLabel());

        errorCorrectionLabel = new JLabel("错误纠正等级: 5级");
        errorCorrectionLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        errorCorrectionLabel.setHorizontalAlignment(JLabel.CENTER);
        
        // 解码相关组件
        uploadImageButton = new JButton("上传图片解码");
        uploadImageButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        uploadImageButton.setBackground(new Color(220, 20, 60));
        uploadImageButton.setForeground(Color.WHITE);
        uploadImageButton.setFocusPainted(false);
        
        decodeResultArea = new JTextArea(8, 50);
        decodeResultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        decodeResultArea.setLineWrap(true);
        decodeResultArea.setWrapStyleWord(true);
        decodeResultArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        decodeResultArea.setEditable(false);
        
        decodeScrollPane = new JScrollPane(decodeResultArea);
        decodeScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        decodeScrollPane.setBorder(BorderFactory.createTitledBorder("解码结果"));
        
        // 缩放滑块
        scaleSlider = new JSlider(25, 300, 100);
        scaleSlider.setMajorTickSpacing(50);
        scaleSlider.setMinorTickSpacing(25);
        scaleSlider.setPaintTicks(true);
        scaleSlider.setPaintLabels(true);
        scaleSlider.setSnapToTicks(false);
        
        scaleSlider.addChangeListener(e -> {
            currentScale = scaleSlider.getValue() / 100.0;
            updateScaleLabel();
            updateBarcodeDisplay();
        });
        
        scaleLabel = new JLabel("缩放比例: 100%");
        scaleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        scaleLabel.setHorizontalAlignment(JLabel.CENTER);
        
        // 去除结束符复选框
        removeEndingCharsCheckBox = new JCheckBox("去除结束符 (GS+RS+EOT)");
        removeEndingCharsCheckBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        removeEndingCharsCheckBox.setSelected(false);
        
        // 从解码结果生成条码按钮
        generateFromDecodeButton = new JButton("从解码结果生成条码");
        generateFromDecodeButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        generateFromDecodeButton.setBackground(new Color(75, 0, 130));
        generateFromDecodeButton.setForeground(Color.WHITE);
        generateFromDecodeButton.setFocusPainted(false);
        generateFromDecodeButton.setEnabled(false);
      }

     private void updateWidthLabel() {
          int width = widthSlider.getValue();
          int height = width / 4; // 高度为宽度的1/4
          widthLabel.setText("条码宽度: " + width + "px (高度: " + height + "px)");
      }

      private void updateColumnsLabel() {
          int columns = columnsSlider.getValue();
          columnsLabel.setText("条码列数: " + columns + "列");
      }

      private void updateErrorCorrectionLabel() {
          int level = errorCorrectionSlider.getValue();
          errorCorrectionLabel.setText("错误纠正等级: " + level + "级");
      }
      
      private void updateScaleLabel() {
          int scale = scaleSlider.getValue();
          scaleLabel.setText("缩放比例: " + scale + "%");
      }
      
      private void updateBarcodeDisplay() {
          if (currentBarcodeImage != null) {
              int newWidth = (int) (currentBarcodeImage.getWidth() * currentScale);
              int newHeight = (int) (currentBarcodeImage.getHeight() * currentScale);
              
              if (newWidth > 0 && newHeight > 0) {
                  Image scaledImage = currentBarcodeImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                  barcodeLabel.setIcon(new ImageIcon(scaledImage));
              }
          }
      }

    private String getDefaultTemplate() {
        return "// PDF417 数据模板 - 请根据需要修改以下内容\n" +
               "// 注释行将被自动过滤，不会包含在最终的条码数据中\n\n" +
               "// === 基本信息 ===\n" +
               "主运单号: 0243201\n" +
               "服务类型: 840\n" +
               "包裹序列号: 804\n" +
               "包裹跟踪号: 288379593956\n\n" +
               "// === 发件人信息 ===\n" +
               "发件人国家代码: FDEG\n" +
               "客户账号: 205025670\n" +
               "包裹类型: 127\n\n" +
               "// === 包裹信息 ===\n" +
               "包裹序号: 1/1\n" +
               "重量: 1.00LB\n" +
               "重量单位: N\n\n" +
               "// === 收件人信息 ===\n" +
               "地址: 85 E 8th Ave\n" +
               "城市: Columbus\n" +
               "州代码: OH\n" +
               "收件人姓名: Will C\n\n" +
               "// === 清关信息 ===\n" +
               "数据子集标识: 06\n" +
               "清关代码: 10ZGH007\n" +
               "海关申报号: 12Z3345241123\n" +
               "填充字段: 20Z\n" +
               "原始跟踪号: 31Z9632080400205025670500288379593956\n" +
               "订单号: 9KOrder #80882231214";
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // 左侧面板 - 输入区域和宽度控制
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(inputScrollPane, BorderLayout.CENTER);
        
        // 条码参数控制面板
        JPanel controlPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        controlPanel.setBorder(BorderFactory.createTitledBorder("条码参数设置"));
        
        // 宽度控制子面板
        JPanel widthPanel = new JPanel(new BorderLayout());
        widthPanel.add(widthLabel, BorderLayout.NORTH);
        widthPanel.add(widthSlider, BorderLayout.CENTER);
        
        // 列数控制子面板
        JPanel columnsPanel = new JPanel(new BorderLayout());
        columnsPanel.add(columnsLabel, BorderLayout.NORTH);
        columnsPanel.add(columnsSlider, BorderLayout.CENTER);
        
        // 错误纠正等级控制子面板
        JPanel errorCorrectionPanel = new JPanel(new BorderLayout());
        errorCorrectionPanel.add(errorCorrectionLabel, BorderLayout.NORTH);
        errorCorrectionPanel.add(errorCorrectionSlider, BorderLayout.CENTER);
        
        controlPanel.add(widthPanel);
        controlPanel.add(columnsPanel);
        controlPanel.add(errorCorrectionPanel);
        
        leftPanel.add(controlPanel, BorderLayout.SOUTH);
        leftPanel.setPreferredSize(new java.awt.Dimension(350, 600));

        // 右侧面板 - 条码显示区域
        JPanel rightPanel = new JPanel(new BorderLayout());
        
        // 条码显示和缩放控制面板
        JPanel barcodePanel = new JPanel(new BorderLayout());
        barcodePanel.add(barcodeScrollPane, BorderLayout.CENTER);
        
        // 缩放控制面板
        JPanel scalePanel = new JPanel(new BorderLayout());
        scalePanel.setBorder(BorderFactory.createTitledBorder("图片缩放"));
        scalePanel.add(scaleLabel, BorderLayout.NORTH);
        scalePanel.add(scaleSlider, BorderLayout.CENTER);
        
        barcodePanel.add(scalePanel, BorderLayout.SOUTH);
        rightPanel.add(barcodePanel, BorderLayout.CENTER);
        
        // 解码区域面板
        JPanel decodePanel = new JPanel(new BorderLayout());
        decodePanel.add(decodeScrollPane, BorderLayout.CENTER);
        
        JPanel decodeButtonPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        decodeButtonPanel.add(uploadImageButton);
        decodeButtonPanel.add(removeEndingCharsCheckBox);
        decodeButtonPanel.add(generateFromDecodeButton);
        decodePanel.add(decodeButtonPanel, BorderLayout.SOUTH);
        
        rightPanel.add(decodePanel, BorderLayout.SOUTH);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(generateButton);
        buttonPanel.add(copyImageButton);
        buttonPanel.add(saveAsButton);
        buttonPanel.add(copyButton);

        // 主面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(350);
        splitPane.setResizeWeight(0.35);

        add(splitPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // 添加说明标签
        JLabel instructionLabel = new JLabel("<html><center>请在左侧编辑内容，点击生成按钮创建PDF417条码<br/>注释行（以//开头）将被自动过滤</center></html>");
        instructionLabel.setHorizontalAlignment(JLabel.CENTER);
        instructionLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(instructionLabel, BorderLayout.NORTH);
    }

    private void setupEventHandlers() {
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateBarcode();
            }
        });

        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyBarcodeData();
            }
        });

        copyImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyBarcodeImage();
            }
        });

        saveAsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveBarcodeImage();
            }
        });
        
        uploadImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                uploadAndDecodeImage();
            }
        });
        
        generateFromDecodeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateFromDecodeResult();
            }
        });
    }

    private void generateBarcode() {
        try {
            String inputText = inputTextArea.getText();
            String processedData = processInputText(inputText);
            
            if (processedData.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入有效的数据内容！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 获取用户设置的宽度和计算高度
            int width = widthSlider.getValue();
            int height = width / 4; // 高度为宽度的1/4
            
            // 获取用户设置的列数
            int columns = columnsSlider.getValue();
            
            // 获取用户设置的错误纠正等级
            int errorCorrectionLevel = errorCorrectionSlider.getValue();
            
            // 生成PDF417条码
            PDF417Writer pdf417Writer = new PDF417Writer();
            
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.CHARACTER_SET, "ISO-8859-1",
                    EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel,
                    EncodeHintType.MARGIN, 10,
                    EncodeHintType.PDF417_DIMENSIONS, new Dimensions(1, columns, 3, 90)
            );

            if (removeEndingCharsCheckBox.isSelected()) {
                processedData = removeEndingChars(processedData);
            }
            JOptionPane.showMessageDialog(this, "processedData:" + processedData, "成功", JOptionPane.INFORMATION_MESSAGE);
            BitMatrix matrix = pdf417Writer.encode(processedData, BarcodeFormat.PDF_417, width, height, hints);
            currentBarcodeImage = createCustomBarcodeImage(matrix);
            
            // 显示条码
            updateBarcodeDisplay();
            
            copyButton.setEnabled(true);
            copyImageButton.setEnabled(true);
            saveAsButton.setEnabled(true);
            
            JOptionPane.showMessageDialog(this, "PDF417条码生成成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (WriterException ex) {
            JOptionPane.showMessageDialog(this, "生成条码时出错: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "发生未知错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String processInputText(String inputText) {
        StringBuilder result = new StringBuilder();
        String[] lines = inputText.split("\n");
        
        // 添加PDF417格式头部
        result.append("[)>").append(RS).append("01").append(GS);
        
        // 用于跟踪字段位置，确保正确处理特殊字段
        int fieldIndex = 0;
        String[] fieldValues = new String[20]; // 预分配足够的空间
        
        // 首先收集所有字段值
        for (String line : lines) {
            line = line.trim();
            
            // 跳过注释行和空行
            if (line.startsWith("//") || line.isEmpty()) {
                continue;
            }
            
            // 处理键值对格式的行
            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    String value = parts[1].trim();
                    if (fieldIndex < fieldValues.length) {
                        fieldValues[fieldIndex] = value;
                        fieldIndex++;
                    }
                }
            } else {
                // 直接添加非键值对格式的行
                if (fieldIndex < fieldValues.length) {
                    fieldValues[fieldIndex] = line;
                    fieldIndex++;
                }
            }
        }
        
        // 按照正确的PDF417格式构建数据
        for (int i = 0; i < fieldIndex; i++) {
            if (fieldValues[i] != null) {
                result.append(fieldValues[i]);
                
                // 特殊处理：包裹类型字段（通常是第6个字段，索引为5）需要两个GS
                if (i == 6) { // 包裹类型字段
                    result.append(GS).append(GS);
                } else if (i == 13) {
                    result.append(RS);
                } else if (i == 17) {
                    result.append(FS).append(GS);
                } else {
                    result.append(GS);
                }
            }
        }
        
        // 添加结束符
        result.append(RS).append(EOT);
        
        return result.toString();
    }

    /**
     * 自定义PDF417条码图片渲染方法
     * 严格按照PDF417标准：每个模块宽高比为1:3
     */
    private BufferedImage createCustomBarcodeImage(BitMatrix matrix) {
        int matrixWidth = matrix.getWidth();
        int matrixHeight = matrix.getHeight();
        
        // 计算模块宽高,保证最终图片宽高比为4:1
        double targetRatio = 4.0; // 目标宽高比
        double currentRatio = (double)matrixWidth / matrixHeight;
        
        // 使用浮点数计算模块大小以保持精确比例
        double moduleWidth, moduleHeight;
        if(currentRatio > targetRatio) {
            // 如果当前比例过宽,调整moduleHeight使其变高
            moduleWidth = 1.0;
            moduleHeight = moduleWidth * currentRatio / targetRatio;
        } else {
            // 如果当前比例过高,调整moduleWidth使其变宽
            moduleHeight = 1.0;
            moduleWidth = moduleHeight * targetRatio / currentRatio;
        }
        
        // 计算最终图片尺寸
        int imageWidth = (int)Math.round(matrixWidth * moduleWidth);
        int imageHeight = (int)Math.round(matrixHeight * moduleHeight);
        
        // 创建BufferedImage
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D graphics = image.createGraphics();
        
        // 设置背景为白色
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, imageWidth, imageHeight);
        
        // 设置前景为黑色
        graphics.setColor(Color.BLACK);
        
        // 逐个渲染矩阵中的每个模块，使用浮点数计算像素位置
        for (int y = 0; y < matrixHeight; y++) {
            for (int x = 0; x < matrixWidth; x++) {
                if (matrix.get(x, y)) {
                    // 使用浮点数计算精确的像素位置和大小
                    int pixelX = (int)Math.round(x * moduleWidth);
                    int pixelY = (int)Math.round(y * moduleHeight);
                    int pixelWidth = (int)Math.round((x + 1) * moduleWidth) - pixelX;
                    int pixelHeight = (int)Math.round((y + 1) * moduleHeight) - pixelY;
                    
                    graphics.fillRect(pixelX, pixelY, pixelWidth, pixelHeight);
                }
            }
        }
        
        graphics.dispose();
        return image;
    }

    private void copyBarcodeData() {
        if (currentBarcodeImage != null) {
            try {
                // 将图像转换为Base64字符串以便复制
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(currentBarcodeImage, "PNG", baos);
                byte[] imageBytes = baos.toByteArray();
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                
                // 复制处理后的数据到剪贴板
                String processedData = processInputText(inputTextArea.getText());
                String copyData = "PDF417条码数据:\n" + processedData + "\n\n" +
                                "Base64图像数据:\n" + base64Image;
                
                StringSelection stringSelection = new StringSelection(copyData);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
                
                JOptionPane.showMessageDialog(this, "条码数据已复制到剪贴板！", "成功", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "复制数据时出错: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "请先生成条码！", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void copyBarcodeImage() {
        if (currentBarcodeImage != null) {
            try {
                // 创建图像传输对象
                ImageTransferable imageTransferable = new ImageTransferable(currentBarcodeImage);
                
                // 复制图像到剪贴板
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(imageTransferable, null);
                
                JOptionPane.showMessageDialog(this, "条码图片已复制到剪贴板！\n可以直接粘贴到其他应用程序中。", "成功", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "复制图片时出错: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "请先生成条码！", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void saveBarcodeImage() {
        if (currentBarcodeImage != null) {
            try {
                // 创建文件选择器
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("保存PDF417条码图片");
                
                // 设置默认文件名
                String defaultFileName = "PDF417_Barcode_" + System.currentTimeMillis() + ".png";
                fileChooser.setSelectedFile(new File(defaultFileName));
                
                // 设置文件过滤器，只允许PNG格式
                FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG图片文件 (*.png)", "png");
                fileChooser.setFileFilter(filter);
                fileChooser.setAcceptAllFileFilterUsed(false);
                
                // 显示保存对话框
                int userSelection = fileChooser.showSaveDialog(this);
                
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();
                    
                    // 确保文件扩展名为.png
                    String filePath = fileToSave.getAbsolutePath();
                    if (!filePath.toLowerCase().endsWith(".png")) {
                        fileToSave = new File(filePath + ".png");
                    }
                    
                    // 检查文件是否已存在
                    if (fileToSave.exists()) {
                        int option = JOptionPane.showConfirmDialog(
                            this,
                            "文件 \"" + fileToSave.getName() + "\" 已存在。\n是否要覆盖它？",
                            "文件已存在",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                        );
                        
                        if (option != JOptionPane.YES_OPTION) {
                            return; // 用户选择不覆盖，退出保存操作
                        }
                    }
                    
                    // 保存图片
                    boolean success = ImageIO.write(currentBarcodeImage, "PNG", fileToSave);
                    
                    if (success) {
                        JOptionPane.showMessageDialog(
                            this,
                            "条码图片已成功保存到:\n" + fileToSave.getAbsolutePath(),
                            "保存成功",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                            this,
                            "保存图片失败！请检查文件路径和权限。",
                            "保存失败",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
                
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "保存图片时出错: " + ex.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE
                );
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "发生未知错误: " + ex.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        } else {
            JOptionPane.showMessageDialog(this, "请先生成条码！", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    // 图像传输类，用于将图像复制到剪贴板
    private static class ImageTransferable implements Transferable {
        private final BufferedImage image;

        public ImageTransferable(BufferedImage image) {
            this.image = image;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.imageFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!DataFlavor.imageFlavor.equals(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return image;
        }
    }

    private void uploadAndDecodeImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择要解码的图片");
        fileChooser.setFileFilter(new FileNameExtensionFilter("图片文件 (*.png, *.jpg, *.jpeg, *.gif, *.bmp)", "png", "jpg", "jpeg", "gif", "bmp"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                decodeImage(selectedFile.getAbsolutePath());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "解码失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                decodeResultArea.setText("解码失败: " + e.getMessage());
            }
        }
    }
    
    private String formatDecodedText(String text) {
        if (text == null) return null;
        
        // 如果选择去除结尾字符，先处理原始文本
        String processedText = text;
        if (removeEndingCharsCheckBox.isSelected()) {
            processedText = removeEndingChars(text);
        }
        
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < processedText.length(); i++) {
            char c = processedText.charAt(i);
            switch (c) {
                case EOT:
                    formatted.append("{EOT}");
                    break;
                case FS:
                    formatted.append("{FS}");
                    break;
                case GS:
                    formatted.append("{GS}");
                    break;
                case RS:
                    formatted.append("{RS}");
                    break;
                default:
                    formatted.append(c);
                    break;
            }
        }
        return formatted.toString();
    }
    
    private String removeEndingChars(String text) {
        if (text == null || text.isEmpty()) return text;
        
        // 去除结尾的 GSRSEOT 序列
        String result = text;
        String endingPattern = "" + GS + RS + EOT;
        if (result.endsWith(endingPattern)) {
            result = result.substring(0, result.length() - endingPattern.length());
        }
        return result;
    }
    
    private void generateFromDecodeResult() {
        String decodeText = decodeResultArea.getText();
        if (decodeText == null || decodeText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "没有解码结果可用于生成条码", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 提取原始内容（第一行通常是原始内容）
        String[] lines = decodeText.split("\n");
        String originalContent = "";
        
        for (String line : lines) {
            if (line.startsWith("原始内容: ")) {
                originalContent = line.substring("原始内容: ".length());
                break;
            }
        }
        
        if (originalContent.isEmpty() && lines.length > 0) {
            // 如果没找到标记的原始内容，使用第一行
            originalContent = lines[0];
        }
        
        if (originalContent.isEmpty()) {
            JOptionPane.showMessageDialog(this, "无法提取有效的内容用于生成条码", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // JOptionPane.showMessageDialog(this, "originalContent:" + formatDecodedText(originalContent), "成功", JOptionPane.INFORMATION_MESSAGE);
        
        // 将内容设置到输入框并直接生成条码（不经过processInputText处理）
        inputTextArea.setText(originalContent);
        generateBarcodeDirectly(originalContent);
        
        JOptionPane.showMessageDialog(this, "已根据解码结果生成新的条码", "成功", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void generateBarcodeDirectly(String rawData) {
        try {
            if (rawData.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入有效的数据内容！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 获取用户设置的宽度和计算高度
            int width = widthSlider.getValue();
            int height = width / 4; // 高度为宽度的1/4
            
            // 获取用户设置的列数
            int columns = columnsSlider.getValue();
            
            // 获取用户设置的错误纠正等级
            int errorCorrectionLevel = errorCorrectionSlider.getValue();
            
            // 生成PDF417条码
            PDF417Writer pdf417Writer = new PDF417Writer();
            
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.CHARACTER_SET, "ISO-8859-1",
                    EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel,
                    EncodeHintType.MARGIN, 10,
                    EncodeHintType.PDF417_DIMENSIONS, new Dimensions(1, columns, 3, 90)
            );

            // JOptionPane.showMessageDialog(this, "rawData:" + rawData, "成功", JOptionPane.INFORMATION_MESSAGE);
            BitMatrix matrix = pdf417Writer.encode(rawData, BarcodeFormat.PDF_417, width, height, hints);
            currentBarcodeImage = createCustomBarcodeImage(matrix);
            
            // 显示条码
            updateBarcodeDisplay();
            
            copyButton.setEnabled(true);
            copyImageButton.setEnabled(true);
            saveAsButton.setEnabled(true);
            
        } catch (WriterException ex) {
            JOptionPane.showMessageDialog(this, "生成条码时出错: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "发生未知错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void decodeImage(String imagePath) throws IOException, NotFoundException {
        GenericMultipleBarcodeReader genericMultipleBarcodeReader = new GenericMultipleBarcodeReader(new MultiFormatReader());

        // 读取图片文件
        BufferedImage bufferedImage = ImageIO.read(new File(imagePath));

        // 将图片转换为LuminanceSource
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);

        // 使用HybridBinarizer构造BinaryBitmap
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        
        decodeResultArea.setText("解码中...");
        StringBuilder resultText = new StringBuilder();
        resultText.append("解码结果:\n\n");
        
        try {
            Result[] results = genericMultipleBarcodeReader.decodeMultiple(bitmap);
            if (results != null && results.length > 0) {
                for (int i = 0; i < results.length; i++) {
                    Result result = results[i];
                    resultText.append("条码 ").append(i + 1).append(":\n");
                    resultText.append("格式: ").append(result.getBarcodeFormat()).append("\n");
                    resultText.append("原始内容: ").append(result.getText()).append("\n");
                    resultText.append("格式化内容: ").append(formatDecodedText(result.getText())).append("\n\n");
                }
            } else {
                resultText.append("未检测到条码");
            }
        } catch (NotFoundException e) {
            // 尝试单个条码解码
            try {
                MultiFormatReader reader = new MultiFormatReader();
                Result singleResult = reader.decode(bitmap);
                resultText.append("条码:\n");
                resultText.append("格式: ").append(singleResult.getBarcodeFormat()).append("\n");
                resultText.append("原始内容: ").append(singleResult.getText()).append("\n");
                resultText.append("格式化内容: ").append(formatDecodedText(singleResult.getText())).append("\n");
            } catch (NotFoundException e2) {
                resultText.append("未检测到条码或条码格式不支持");
            }
        }
        
        decodeResultArea.setText(resultText.toString());
        
        // 启用从解码结果生成条码按钮
        generateFromDecodeButton.setEnabled(true);
        
        JOptionPane.showMessageDialog(this, "解码完成！", "成功", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new PDF417GeneratorGUI().setVisible(true);
            }
        });
    }
}