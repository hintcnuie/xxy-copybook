package io.github.radium0028.xxycopybook;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReflectUtil;
import io.github.radium0028.xxycopybook.cell.AbstractCell;
import io.github.radium0028.xxycopybook.cell.ConretetCell;
import io.github.radium0028.xxycopybook.material.CopybookData;
import io.github.radium0028.xxycopybook.material.CopybookTemplate;
import io.github.radium0028.xxycopybook.text.CellPinyin;
import io.github.radium0028.xxycopybook.text.CellPinyinCopy;
import io.github.radium0028.xxycopybook.text.CellText;
import io.github.radium0028.xxycopybook.text.CellTextCopy;
import io.github.radium0028.xxycopybook.utils.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 一个基础的字帖建造者
 *
 * @author radium
 */
public class BaseCopybook extends AbstractCopybookBuilder {
    private static final Logger logger = LoggerFactory.getLogger(BaseCopybook.class);

    int textCellWidth = this.templateBean.getTextCellWidth();
    int textCellHeight = this.templateBean.getTextCellHeight();
    int pinyinCellWidth = this.templateBean.getPinyinCellWidth();
    int pinyinCellHeight = this.templateBean.getPinyinCellHeight();

    public BaseCopybook(CopybookTemplate copybookTemplate, CopybookData copybookData) {
        super(copybookTemplate, copybookData);
    }

    /**
     * 创建页面骨架
     */
    @Override
    public BufferedImage createBasic() {
        //一个白色的底图
        logger.debug("default background image:一个白色的底图");
        return ImageUtil.createImage(this.templateBean.getWidth(), this.templateBean.getHeight(), Color.WHITE);
    }

    @Override
    public AbstractCell createTextCell() {
        AbstractCell abstractCell = new ConretetCell(this.templateBean.getTextCellWidth(),
                templateBean.getTextCellHeight());

        AtomicReference<AbstractCell> atomCell = new AtomicReference<>(abstractCell);
        this.templateBean.getTextCellLineStyle().forEach(lineStyle -> {
            try {
                //应用各类装饰器
                Class clz = Class.forName(lineStyle.getValue());
                //线条颜色
                Map<String, Color> stringColorMap =
                        Optional.ofNullable(this.templateBean.getTextLineColorMap()).orElse(new HashMap<>());
                Color color = stringColorMap.get(lineStyle.getValue());
                color = Optional.ofNullable(color).orElse(this.templateBean.getTextLineColor());
                //线条样式
                Map<String, BasicStroke> lineStrokeMap =
                        Optional.ofNullable(this.templateBean.getTextLineStrokeMap()).orElse(new HashMap<>());
                BasicStroke stroke = lineStrokeMap.get(lineStyle.getValue());
                stroke = Optional.ofNullable(stroke).orElse(this.templateBean.getTextLineStroke());
                //创建装饰器
                atomCell.set((AbstractCell) ReflectUtil.newInstance(clz, atomCell.get(), color, stroke));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });

        return atomCell.get();
    }

    @Override
    public AbstractCell createPinyinCell() {
        if (this.templateBean.isShowPinyin()) {
            AbstractCell abstractCell = new ConretetCell(this.templateBean.getPinyinCellWidth(),
                    templateBean.getPinyinCellHeight());
            AtomicReference<AbstractCell> atomCell = new AtomicReference<>(abstractCell);
            this.templateBean.getPinyinCellLineStyle().forEach(lineStyle -> {
                try {
                    //应用各类装饰器
                    Class clz = Class.forName(lineStyle.getValue());
                    //线条颜色
                    Map<String, Color> pinyinLineColorMap =
                            Optional.ofNullable(this.templateBean.getPinyinLineColorMap()).orElse(new HashMap<>());
                    Color color = pinyinLineColorMap.get(lineStyle.getValue());
                    color = Optional.ofNullable(color).orElse(this.templateBean.getPinyinLineColor());
                    //线条样式
                    Map<String, BasicStroke> pinyinLineStrokeMap =
                            Optional.ofNullable(this.templateBean.getPinyinLineStrokeMap()).orElse(new HashMap<>());
                    BasicStroke stroke = pinyinLineStrokeMap.get(lineStyle.getValue());
                    stroke = Optional.ofNullable(stroke).orElse(this.templateBean.getPinyinLineStroke());

                    //创建装饰器
                    atomCell.set((AbstractCell) ReflectUtil.newInstance(clz, atomCell.get(), color, stroke));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
            return atomCell.get();
        }
        return null;
    }

    @Override
    public List<RowData> createRow(AbstractCell textAbstractCell, AbstractCell pinyinAbstractCell) throws Exception {
        //每行有多少个字
        int rowTextNum = this.templateBean.getRowCellNum();
        //一共有多少个字
        int textNum = this.copybookData.getWordList().size();
        //完整的文字每行几个字
        int fullWordNum = this.templateBean.getFullWordNum();
        //描红每行几个字
        int copyWordNum = this.templateBean.getCopyWordNum();
        //每行空白的个需要几个
        int emptyCellNum = this.templateBean.getEmptyCellNum();

        if (fullWordNum + copyWordNum + emptyCellNum > rowTextNum) {
            throw new Exception("每行文字的数量小于完整+描红+空白的数量");
        }
        List<RowData> rowDataList = CollUtil.newArrayList();
        //计算需要多少行
        int rowCount = (int) Math.ceil(NumberUtil.div(textNum * (fullWordNum + copyWordNum + emptyCellNum),
                rowTextNum));

        for (int i = 0; i < rowCount; i++) {
            for (int z = 0; z < rowTextNum; z++) {
                RowData rowData = new RowData();
                // 每行几个字 * 当前行索引 + 当前循环到第几个行中的字（也就是列）
                int wordIndex = rowTextNum * i + z;
                if (this.copybookData.getWordList().size() <= wordIndex) {
                    break;
                }
                String word = this.copybookData.getWordList().get(wordIndex);
//                log.debug("准备画文字：{}", word);
                //开始画一行的文字
                for (int j = 0; j < fullWordNum; j++) {
//                    log.debug("画'{}'的完整文字：", word);
                    //完整文字
                    CellText cellText = new CellText(textAbstractCell, this.templateBean.getFont(),
                            this.templateBean.getTextColor());
                    cellText.setText(word);
                    if (this.templateBean.isShowPinyin()) {
                        CellPinyin pinyinCell1 = new CellPinyin(pinyinAbstractCell, this.templateBean.getPinyinFont()
                                , this.templateBean.getPinyinColor());
                        pinyinCell1.setText(this.copybookData.getPinyinList().get(wordIndex));
                        rowData.push(cellText, pinyinCell1);
                    } else {
                        rowData.push(cellText);
                    }
                }

                for (int x = 0; x < copyWordNum; x++) {
//                    log.debug("画'{}'的描红文字：", word);
                    //描红文字
                    CellTextCopy textCell1 = new CellTextCopy(textAbstractCell, this.templateBean.getFont(),
                            this.templateBean.getTextColor());
                    textCell1.setText(word);
                    if (this.templateBean.isShowPinyin()) {
                        CellPinyinCopy pinyinCell1 = new CellPinyinCopy(pinyinAbstractCell,
                                this.templateBean.getPinyinFont()
                                , this.templateBean.getPinyinColor());
                        pinyinCell1.setText(this.copybookData.getPinyinList().get(wordIndex));
                        rowData.push(textCell1, pinyinCell1);
                    } else {
                        rowData.push(textCell1);
                    }
                }

                for (int y = 0; y < emptyCellNum; y++) {
//                    log.debug("画'{}'的空白格：", word);
                    //空白个
                    if (this.templateBean.isShowPinyin()) {
                        rowData.push(textAbstractCell, pinyinAbstractCell);
                    } else {
                        rowData.push(textAbstractCell);
                    }
                }
                rowDataList.add(rowData);
            }
        }
        return rowDataList;
    }

    @Override
    public List<BufferedImage> builderRow(List<RowData> rowData) {
        if (rowData == null || rowData.size() <= 0) {
            return null;
        }
        List<BufferedImage> resultImage = CollUtil.newArrayList();
        //计算行的高度，
        int columnHeight =
                this.templateBean.getTextCellHeight() + this.templateBean.getCellMarginTop() + this.templateBean.getCellMarginBottom();
        if (this.templateBean.isShowPinyin()) {
            //如果有拼音格，再加上拼音格的高度
            columnHeight = columnHeight + this.templateBean.getPinyinCellHeight();
        }
        //计算行的宽度
        //在这里根据列宽计算出整个图像的尺寸。(列宽+间距)*列数量
        int columnWidth =
                this.templateBean.getTextCellWidth() + this.templateBean.getCellMarginLeft() + this.templateBean.getCellMarginRight();
        columnWidth = columnWidth * this.templateBean.getRowCellNum();

        //先画一个行的底图，依然是透明底的。
        BufferedImage finalRowImage = ImageUtil.createImage(columnWidth, columnHeight);
        CollUtil.forEach(rowData, (row, index) -> {
            //每行里有多少列
            int columnCount = row.columnCount();
            //创建一行的画板
            BufferedImage image = ImageUtil.copyImage(finalRowImage);
            Graphics2D g = image.createGraphics();
            //循环列数
            for (int i = 0; i < columnCount; i++) {
                AbstractCell[] abstractCells = row.pull(i);
                if (abstractCells != null) {
                    AbstractCell text = abstractCells[0];
                    if (text == null) {
                        continue;
                    }
                    int offsetPinyinX = this.pinyinCellWidth + this.templateBean.getCellMarginLeft();
                    int offsetTextX = this.textCellWidth + this.templateBean.getCellMarginLeft();

////                    这里的代码还不完整，先不启用
//                    if(this.templateBean.getCellMarginLeft()==0){
//                        // 如果边距是0，则认为是希望合并在一起的，偏移量减一条外边的宽度.只对第二列开始生效。
//                        // 这里不知道怎么取边框的宽度了~~~
//                        if( i > 0 ){
//                            offsetPinyinX = offsetPinyinX-2;
//                            offsetTextX = offsetTextX-2;
//                        }
//                    }

                    if (this.templateBean.isShowPinyin() && abstractCells.length >= 2) {
                        //有拼音格，先画拼音，再画文字
                        if (abstractCells[1] != null) {
                            g.drawImage(abstractCells[1].draw(),
                                    offsetPinyinX * i,
                                    this.templateBean.getCellMarginTop(), null);
                        }
                        g.drawImage(text.draw(), offsetTextX * i,
                                this.pinyinCellHeight + this.templateBean.getCellMarginTop(), null);
                    } else if (abstractCells.length >= 1) {
                        g.drawImage(text.draw(), offsetTextX * i,
                                this.templateBean.getCellMarginTop(), null);
                    }
                }
            }
            g.dispose();
            resultImage.add(image);
        });
        return resultImage;
    }

    @Override
    public BufferedImage createHeader() {
        if (this.templateBean.getHeaderHeight() > 0) {
            AbstractCell abstractCell = new ConretetCell(this.templateBean.getBackgroundColor(),
                    this.templateBean.getWidth(),
                    this.templateBean.getHeaderHeight());
            CellText textCell = new CellText(abstractCell, this.templateBean.getFont(),
                    this.templateBean.getTextColor());
            String xstr = Optional.ofNullable(this.copybookData.getTitle()).orElse("X字帖自动生成");
            textCell.setText(xstr);
            BufferedImage image = textCell.draw();
            //给image右下角添加文字
            Graphics2D g = image.createGraphics();
            g.setColor(Color.BLACK);
            Font font = new Font(this.templateBean.getFont().getName(), Font.BOLD, 50);
            g.setFont(font);
            g.drawString(this.copybookData.getSubtitle(), this.templateBean.getWidth() - 700,
                    this.templateBean.getHeaderHeight() - 50);
            g.dispose();
            return image;
        } else {
            return null;
        }
    }

    @Override
    public BufferedImage createFooter() {
        if (this.templateBean.getFooterHeight() > 0) {
            BufferedImage bufferedImage = ImageUtil.createImage(this.templateBean.getWidth(),
                    this.templateBean.getFooterHeight(), this.templateBean.getBackgroundColor());
            //给image右下角添加文字
            Graphics2D graphics = bufferedImage.createGraphics();
            graphics.setColor(Color.BLACK);
            Font font = new Font(this.templateBean.getFont().getName(), Font.PLAIN, 50);
            graphics.setFont(font);
            graphics.drawString((this.copybookData.getCopyright() != null ? this.copybookData.getCopyright() : "")
                            + (this.copybookData.getAuthor() != null ? this.copybookData.getAuthor() : "") + " "
                            + DateUtil.today(),
                    this.templateBean.getWidth() - 500,
                    this.templateBean.getHeaderHeight() - 50);
            graphics.dispose();
            return bufferedImage;
        } else {
            return null;
        }
    }

    /**
     * 返回页面图像
     *
     * @param basicImage  页面底图框架
     * @param headerImage 头部信息
     * @param footerImage 尾部信息
     * @param rowsList    每行的字帖信息
     * @return
     */
    @Override
    public List<BufferedImage> builderPage(BufferedImage basicImage, BufferedImage headerImage,
                                           BufferedImage footerImage, List<BufferedImage> rowsList) {
        List<BufferedImage> list = CollUtil.newArrayList();
        //页面的整体高度
        int basicImageHeight = basicImage.getHeight();
        //当前是第几页
        AtomicInteger pageIndex = new AtomicInteger();
        //页面图像
        AtomicReference<BufferedImage> pageImage = new AtomicReference<>();
        //记录在当前页面上的函数索引，在每次新建页面时会被设置为0
        AtomicInteger pageRowIndex = new AtomicInteger();

        //TODO 这里可以考虑来个自动居中

        //正文区域的坐标系，页面的上边距 + 页面头部尺寸 + 页面头部边距
        int pageStartX = this.templateBean.getPagePaddingLeft();
        int pageStartY = this.templateBean.getPagePaddingTop() +
                this.templateBean.getHeaderHeight() +
                this.templateBean.getHeaderMarginBottom();

        int pageEndX = this.templateBean.getPagePaddingRight();
        int pageEndY = basicImageHeight -
                this.templateBean.getPagePaddingBottom() -
                this.templateBean.getFooterHeight() -
                this.templateBean.getFooterMarginTop();

        //计算一下每个页面可以放几行
        int pageRowCount =
                (pageEndY - pageStartY) / this.templateBean.getTextCellHeight() + this.templateBean.getCellMarginTop();

        CollUtil.forEach(rowsList, (row, index) -> {
            BufferedImage pageImageCom = pageImage.get();
            Graphics2D graphics = null;
            if (pageImageCom != null) {
                graphics = pageImageCom.createGraphics();
            }

            //判断是否需要新建页面，通过高度是否超过当前页面的方式做判断。
            //行高*页面数量的索引 + 行高 = 当前行所需要的高度
            // 如果 当前行所需要的高度 < 页面的高度 ，则需要新建一页
            // pageIndex记录当前页面的索引，如果他是0，则表示第一页，也需要新建页面。
            if (pageIndex.get() == 0 || pageRowIndex.get() > pageRowCount) {
                //如果不是第一次，需要把上一次的页面保存起来。
                if (pageIndex.get() != 0 && pageImage.get() != null) {
                    list.add(pageImage.get());
                }
                //新建页面
                pageImage.set(ImageUtil.copyImage(basicImage));
                pageIndex.getAndIncrement();
                pageRowIndex.set(0);
                //画头部内容
                graphics = pageImage.get().createGraphics();
                if (headerImage != null) {
                    graphics.drawImage(headerImage, 0, 0, null);
                }
                //画尾部内容
                if (footerImage != null) {
                    graphics.drawImage(footerImage, 0, basicImageHeight - footerImage.getHeight(),
                            null);
                }
            }

            //新行的x 需要增加页面偏移量，也就是边距。
            //新行的y 页面的行索引 * 行高 + 行间距
            int offsexY = pageStartY + pageRowIndex.get() * row.getHeight();
            pageRowIndex.getAndIncrement();
//          这里的代码还不完整，先不启用
//            if(this.templateBean.getCellMarginBottom()==0 && this.templateBean.getCellMarginTop() == 0){
//                //如果上边距是0，则认为是希望合并在一起的，高度减一个外边框的厚度。只对第一行开始的生效。
//                // 和左边距一样，不知道怎么去外边框的后端了。
//                if(index > 0){
//                    offsexY = offsexY - 2;
//                }
//            }
            graphics.drawImage(row, pageStartX,
                    offsexY, null);
            graphics.dispose();
        });
        list.add(pageImage.get());
        return list;
    }
}