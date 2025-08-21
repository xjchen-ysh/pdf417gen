# PDF417条码宽度调整功能

## 功能概述
为PDF417条码生成器添加了用户可调整的宽度控制功能，允许用户通过滑块自定义条码的尺寸。

## 功能特性

### 1. 宽度调整滑块
- **范围**: 800px - 2400px
- **默认值**: 1200px
- **步进**: 确保值为4的整倍数
- **实时预览**: 滑块值变化时实时显示当前尺寸

### 2. 自动高度计算
- **计算规则**: 高度 = 宽度 ÷ 4
- **示例**: 
  - 宽度1200px → 高度300px
  - 宽度800px → 高度200px
  - 宽度2400px → 高度600px

### 3. 用户界面改进
- **位置**: 左侧面板底部
- **标题**: "条码尺寸设置"
- **显示**: 实时显示当前宽度和计算的高度
- **样式**: 带有刻度标记的滑块控件

## 技术实现

### 1. 组件添加
```java
// 新增组件
private JSlider widthSlider;
private JLabel widthLabel;

// 滑块初始化
widthSlider = new JSlider(800, 2400, 1200);
widthSlider.setMajorTickSpacing(400);
widthSlider.setMinorTickSpacing(100);
widthSlider.setPaintTicks(true);
widthSlider.setPaintLabels(true);
```

### 2. 值约束处理
```java
// 确保滑块值是4的整倍数
widthSlider.addChangeListener(e -> {
    int value = widthSlider.getValue();
    int adjustedValue = (value / 4) * 4;
    if (adjustedValue != value) {
        widthSlider.setValue(adjustedValue);
    }
    updateWidthLabel();
});
```

### 3. 条码生成集成
```java
// 获取用户设置的宽度和计算高度
int width = widthSlider.getValue();
int height = width / 4;

// 应用到条码生成
BitMatrix bitMatrix = writer.encode(processedText, BarcodeFormat.PDF_417, width, height);
```

## 用户体验

### 1. 操作流程
1. 用户在左侧面板底部看到"条码尺寸设置"区域
2. 拖动滑块调整宽度值（800-2400px）
3. 标签实时显示当前宽度和对应高度
4. 点击"生成条码"按钮应用新尺寸
5. 条码以用户设定的尺寸生成

### 2. 智能约束
- 自动调整为4的整倍数
- 防止无效尺寸设置
- 保持宽高比例一致

### 3. 视觉反馈
- 滑块带有主要和次要刻度
- 实时显示数值标签
- 清晰的面板分组

## 文件更新

### 修改的文件
- `src/main/java/com/example/demo/PDF417GeneratorGUI.java`
  - 添加滑块组件声明
  - 实现滑块初始化和事件处理
  - 修改布局添加宽度控制面板
  - 更新条码生成方法使用动态尺寸

### 生成的文件
- `target/PDF417Generator.exe` - 包含新功能的可执行文件
- `target/PDF417Generator.jar` - 包含新功能的JAR文件

## 版本信息
- **版本**: 1.0.0
- **更新日期**: 2025-08-11
- **新增功能**: 宽度调整滑块
- **兼容性**: 保持所有原有功能

## 测试验证

### 功能测试
1. ✅ 滑块范围验证（800-2400px）
2. ✅ 4的整倍数约束验证
3. ✅ 高度自动计算验证
4. ✅ 实时标签更新验证
5. ✅ 条码生成尺寸应用验证

### 界面测试
1. ✅ 滑块控件显示正常
2. ✅ 标签文字清晰可读
3. ✅ 面板布局合理
4. ✅ 与其他控件协调

## 用户价值
- **灵活性**: 用户可根据需要调整条码尺寸
- **便利性**: 直观的滑块操作方式
- **准确性**: 自动确保尺寸规范
- **实时性**: 即时查看尺寸效果