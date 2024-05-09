package io.github.radium.xxycopybook;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import io.github.radium0028.xxycopybook.BaseCopybook;
import io.github.radium0028.xxycopybook.Copybook;
import io.github.radium0028.xxycopybook.CopybookDirector;
import io.github.radium0028.xxycopybook.cell.AbstractCellDecorator;
import io.github.radium0028.xxycopybook.cell.StrokeForCell;
import io.github.radium0028.xxycopybook.dict.LineStyle;
import io.github.radium0028.xxycopybook.material.CopybookData;
import io.github.radium0028.xxycopybook.material.CopybookTemplate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Unit test for simple App.
 */
public class YiNianJiXia
{
    private static final String outputPath = AbstractCellDecorator.class.getClassLoader().getResource("fonts").getFile() +"/../../output/";
    private static final Logger logger = LoggerFactory.getLogger(YiNianJiXia.class);
    @BeforeAll
    static void before() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        //注册所有字体文件
        URL fonts = AbstractCellDecorator.class.getClassLoader().getResource("fonts");
        try {
            logger.debug("fonts.getFile(): {}", fonts.getFile());
            logger.debug("font path: {}", fonts.toURI().getRawPath());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        String file = fonts.getFile();
        File fontsPath = new File(fonts.getFile());

        Optional.ofNullable(fontsPath).ifPresent(fp -> {
            Arrays.stream(fp.listFiles()).forEach(fontFile -> {
                try {
                    Font font = Font.createFont(Font.PLAIN, fontFile);
                    ge.registerFont(font);
                } catch (FontFormatException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
        boolean isHeadless = ge.isHeadlessInstance();
        logger.debug("GraphicsEnvironment headless?"+isHeadless);

        Font[] availableFonts = ge.getAllFonts();
        logger.debug("============Fonts in GraphicsEnvironment  are "+availableFonts.length + "============");

        for (Font font : availableFonts) {
            String fontName = font.getFontName();
           // logger.debug(fontName);
        }
        logger.debug("==============================end before（）===============");
    }

    /**
     * 测试画页头和页尾
     */
    @Test
    void constructHF_14() {
        //需要些的字
        String text = "直,呀,边,呢,吗,吧,加";
        String pinyin = "zhí,yā,biān,ne,má,bā,jiā";
        //字体名字
        String fontName = "方正仿宋-简体";
        CopybookTemplate.CopybookTemplateBuilder copybookTemplateBuilder = CopybookTemplate.builder()
                .textLineStroke(StrokeForCell.DOTTED_LINE)
                .cellMargin(new Integer[]{10, 0, 0, 0})
                //单元格使用一个边框+田字格样式。
                .textCellLineStyle(CollUtil.toList(LineStyle.BORDER, LineStyle.TIAN));

        //设置页头高度为80
        copybookTemplateBuilder.headerHeight(200);
        //设置页尾高度为50
        copybookTemplateBuilder.footerHeight(200);

        //拼音设置
        copybookTemplateBuilder.showPinyin(true)
                .pinyinFont(new Font("Mengshen-Handwritten",Font.PLAIN,50))
                .pinyinCellLineStyle(CollUtil.toList(LineStyle.BORDER, LineStyle.PINYINCELL));
        copybookTemplateBuilder.pinyinLineStrokeMap(MapUtil
                .builder(LineStyle.BORDER.getValue(), StrokeForCell.LINE_BOLD)
                .put(LineStyle.PINYINCELL.getValue(), StrokeForCell.DOTTED_LINE)
                .put(LineStyle.XCELL.getValue(), StrokeForCell.DOTTED_LINE)
                .build()
        );
        //给边框格一个加粗的边线
        copybookTemplateBuilder.textLineStrokeMap(MapUtil
                .builder(LineStyle.BORDER.getValue(), StrokeForCell.LINE_BOLD)
                .build());
        //设置字体
        copybookTemplateBuilder.font(new Font(fontName, Font.PLAIN, 140));
        //设置模板数据
        CopybookTemplate copybookTemplate = copybookTemplateBuilder.pagePadding(new Integer[]{10,10,10,200}).build();
        CopybookData copybookData = CopybookData.builder()
                .title("一年级下-14")
                .wordList(CollUtil.toList(text.split(",")))
                .pinyinList(CollUtil.toList(pinyin.split(",")))
                .build();

        BaseCopybook baseCopybook = new BaseCopybook(copybookTemplate, copybookData);
        CopybookDirector director = new CopybookDirector(baseCopybook);
        try {
            Copybook construct = director.buildCopybook();
            List<BufferedImage> bufferedImage = construct.exportImage();
            CollUtil.forEach(bufferedImage, (v, i) -> {
                //输出图像
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                try {
                    ImageIO.write(v, "png", output);

                    File constructFile = new File(outputPath +
                            StrUtil.format("constructHeaderAndFooter{}.png", i));
                    FileUtil.writeBytes(output.toByteArray(), constructFile);
                    logger.debug("Write file to " + constructFile.getAbsolutePath());
                    Desktop.getDesktop().open(constructFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }
    /**
     * 一年级下语文-14课
     */
    @Test
    void construct_14() {
        //需要些的字
        String text = "直呀边呢吗吧加";
        //字体名字
        String fontName = "方正仿宋-简体";

        CopybookTemplate.CopybookTemplateBuilder copybookTemplateBuilder = CopybookTemplate.builder()
                .emptyCellNum(2)
                .textLineStroke(StrokeForCell.LINE)
                //单元格使用一个边框+田字格样式。
                .textCellLineStyle(CollUtil.toList(LineStyle.BORDER, LineStyle.TIAN))
                //.textCellLineStyle(CollUtil.toList(LineStyle.BORDER, LineStyle.PINYIN3CELL))
                ;
        //给边框格一个加粗的边线
        copybookTemplateBuilder.textLineStrokeMap(MapUtil
                .builder(LineStyle.BORDER.getValue(), StrokeForCell.LINE_BOLD)

                .build());
        Font font = new Font(fontName, Font.PLAIN, 140);
        copybookTemplateBuilder.font(font);
        //设置模板数据
        CopybookTemplate copybookTemplate = copybookTemplateBuilder.pagePadding(new Integer[]{10,10,10,200}).build();
        CopybookData copybookData = CopybookData.builder()
                .author("Radium")
                .wordList(CollUtil.toList(text.split("")))
                .build();

        BaseCopybook baseCopybook = new BaseCopybook(copybookTemplate, copybookData);
        CopybookDirector director = new CopybookDirector(baseCopybook);
        try {
            Copybook construct = director.buildCopybook();
            BufferedImage bufferedImage = construct.exportFirstImage();
            //输出图像
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", output);
            File constructFile = new File(outputPath+"construct.png");
            FileUtil.writeBytes(output.toByteArray(), constructFile);
            logger.debug("Write file to " + constructFile.getAbsolutePath());
            Desktop.getDesktop().open(constructFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}