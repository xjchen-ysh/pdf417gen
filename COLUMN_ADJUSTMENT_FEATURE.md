# PDF417 列数调整功能

## 概述
本文档描述了为PDF417条码生成器添加的列数调整功能。该功能允许用户通过滑块控制PDF417条码的列数，提供更灵活的条码生成选项。

## 功能特性

### 列数调整滑块
- **范围**: 1-30列
- **默认值**: 10列
- **实时预览**: 滑块值变化时立即更新显示标签
- **约束**: 整数值，步长为1

### 自动尺寸计算
- 列数通过滑块设置
- 行数由ZXing库根据数据内容和列数自动计算
- 使用Dimensions类指定最小和最大行列数范围

### 实时显示
- 显示当前选择的列数
- 格式："列数: X"

## 界面改进

### 新增控制区域
- 在左侧面板添加了"条码尺寸设置"区域
- 包含宽度调整和列数调整两个控制组
- 使用GridLayout布局，整齐排列控制元素

### 滑块设计
- 带有刻度标记的滑块
- 主要刻度间隔为5
- 次要刻度间隔为1
- 显示刻度标签

## 技术实现

### 组件添加
```java
// 列数调整组件
private JSlider columnsSlider;
private JLabel columnsLabel;
```

### 初始化设置
```java
// 列数滑块初始化
columnsSlider = new JSlider(1, 30, 10);
columnsSlider.setMajorTickSpacing(5);
columnsSlider.setMinorTickSpacing(1);
columnsSlider.setPaintTicks(true);
columnsSlider.setPaintLabels(true);

// 添加变化监听器
columnsSlider.addChangeListener(e -> updateColumnsLabel());
```

### 事件处理
- 使用ChangeListener监听滑块值变化
- 实时更新列数标签显示
- 调用updateColumnsLabel()方法更新界面

### 条码生成集成
```java
// 在generateBarcode方法中使用列数
int columns = columnsSlider.getValue();
Map<EncodeHintType, Object> hints = Map.of(
    EncodeHintType.CHARACTER_SET, "ISO-8859-1",
    EncodeHintType.ERROR_CORRECTION, 5,
    EncodeHintType.MARGIN, 10,
    EncodeHintType.PDF417_DIMENSIONS, new Dimensions(columns, columns, 3, 90)
);
```

### 标签更新方法
```java
private void updateColumnsLabel() {
    int columns = columnsSlider.getValue();
    columnsLabel.setText("列数: " + columns);
}
```

## 更新的文件

### PDF417GeneratorGUI.java
- 添加了columnsSlider和columnsLabel组件声明
- 在initializeComponents方法中初始化列数控制组件
- 添加了updateColumnsLabel方法
- 修改了setupLayout方法以包含列数控制面板
- 更新了generateBarcode方法以使用PDF417_DIMENSIONS参数
- 添加了Dimensions类的导入

### 可执行文件
- PDF417Generator.exe - 包含列数调整功能的Windows可执行文件
- PDF417Generator.jar - 包含列数调整功能的Java归档文件

## 用户体验

### 操作简便
- 直观的滑块控制
- 实时反馈显示
- 无需手动输入数值

### 即时反馈
- 滑块移动时立即更新标签
- 生成条码时应用当前列数设置
- 视觉化的列数选择过程

### 灵活定制
- 支持1-30列的广泛范围
- 适应不同数据量和显示需求
- 与宽度调整功能协同工作

## 技术细节

### ZXing库集成
- 使用PDF417_DIMENSIONS参数替代不存在的PDF417_COLUMNS
- 通过Dimensions类指定列数范围
- 构造函数参数：Dimensions(minCols, maxCols, minRows, maxRows)
- 设置为：new Dimensions(columns, columns, 3, 90)

### 参数说明
- minCols和maxCols设置为相同值以固定列数
- minRows设置为3，确保最小行数
- maxRows设置为90，允许足够的行数容纳数据

## 版本信息
- **版本**: 1.0.0
- **添加日期**: 2025年8月
- **兼容性**: Java 8+
- **依赖**: ZXing 3.5.2

## 测试验证

### 功能测试
- ✅ 滑块值变化正确更新标签
- ✅ 列数设置正确应用到条码生成
- ✅ 界面布局正常显示
- ✅ 与宽度调整功能协同工作

### 兼容性测试
- ✅ 编译成功无错误
- ✅ 运行时无异常
- ✅ 条码生成正常
- ✅ 可执行文件正常工作

---

*此功能为PDF417条码生成器提供了更强的定制能力，用户可以根据具体需求调整条码的列数，优化条码的显示效果和数据密度。*