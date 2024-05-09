package io.github.radium0028.xxycopybook;

import io.github.radium0028.xxycopybook.cell.AbstractCell;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * @author radium
 */
@AllArgsConstructor
public class CopybookDirector {
    private static final Logger logger = LoggerFactory.getLogger(CopybookDirector.class);

    @Setter
    AbstractCopybookBuilder abstractCopybookBuilder;

    public Copybook buildCopybook() throws Exception {
        //一个基础模板样式
        BufferedImage basic = abstractCopybookBuilder.createBasic();
        //header & footer
        BufferedImage header = abstractCopybookBuilder.createHeader();
        BufferedImage footer = abstractCopybookBuilder.createFooter();
        //text cell (田字格）
        AbstractCell textAbstractCell = abstractCopybookBuilder.createTextCell();
        //Pinyin cell （三线拼音格）
        AbstractCell pinyinAbstractCell = abstractCopybookBuilder.createPinyinCell();

        //行数据
        List<RowData> rowDataList = abstractCopybookBuilder.createRow(textAbstractCell, pinyinAbstractCell);
        List<BufferedImage> rowImage = abstractCopybookBuilder.builderRow(rowDataList);

        //创建页面Image
        List<BufferedImage> bufferedImages = abstractCopybookBuilder.builderPage(basic, header, footer, rowImage);

        abstractCopybookBuilder.copybook = new Copybook();
        abstractCopybookBuilder.copybook.setPinyinAbstractCell(pinyinAbstractCell);
        abstractCopybookBuilder.copybook.setTextAbstractCell(textAbstractCell);
        abstractCopybookBuilder.copybook.setBufferedImage(bufferedImages);

        return abstractCopybookBuilder.build();
    }
}