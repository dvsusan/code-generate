package com.sue.generate.service.impl;


import com.sue.generate.config.GenConfig;
import com.sue.generate.domain.ColumnInfo;
import com.sue.generate.domain.TableInfo;
import com.sue.generate.mapper.GenMapper;
import com.sue.generate.service.IGenService;
import com.sue.generate.util.CharsetKit;
import com.sue.generate.util.GenUtils;
import com.sue.generate.util.VelocityInitializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成 服务层处理
 *
 * @author sue
 */
@Slf4j
@Service
public class GenServiceImpl implements IGenService {

    @Autowired
    private GenMapper genMapper;

    /**
     * 查询ry数据库表信息
     *
     * @param tableInfo 表信息
     * @return 数据库表列表
     */
    @Override
    public List<TableInfo> selectTableList(TableInfo tableInfo) {
        return genMapper.selectTableList(tableInfo);
    }

    /**
     * 生成代码
     *
     * @param author       作者
     * @param packageName  包名称
     * @param dataBaseName 库名称
     * @param tableName    表名称
     * @return 数据
     */
    @Override
    public byte[] generatorCode(String author, String packageName, String dataBaseName, String tableName) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        generatorCode(author, packageName, dataBaseName, tableName, zip);
        IOUtils.closeQuietly(zip);
        return outputStream.toByteArray();
    }

    /**
     * 批量生成代码
     *
     * @param author       作者
     * @param packageName  包名称
     * @param dataBaseName 库名称
     * @param tableNames   表数组
     * @return 数据
     */
    @Override
    public byte[] generatorCode(String author, String packageName, String dataBaseName, String[] tableNames) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        for (String tableName : tableNames) {
            generatorCode(author, packageName, dataBaseName, tableName, zip);
        }
        IOUtils.closeQuietly(zip);
        return outputStream.toByteArray();
    }

    /**
     * 查询表信息并生成代码
     */
    private void generatorCode(String author, String packageName, String dataBaseName, String tableName, ZipOutputStream zip) {
        // 查询表信息
        TableInfo table = genMapper.selectTableByName(dataBaseName, tableName);
        // 查询列信息
        List<ColumnInfo> columns = genMapper.selectTableColumnsByName(dataBaseName, tableName);

        // 表名转换成Java属性名
        String className = GenUtils.tableToJava(table.getTableName());
        table.setClassName(className);
        table.setClassname(StringUtils.uncapitalize(className));
        // 列信息
        table.setColumns(GenUtils.transColums(columns));
        // 设置主键
        table.setPrimaryKey(table.getColumnsLast());

        VelocityInitializer.initVelocity();

        String realPackageName;
        if (StringUtils.isEmpty(packageName)) {
            realPackageName = GenConfig.getPackageName();
        } else {
            realPackageName = packageName;
        }
        String moduleName = GenUtils.getModuleName(realPackageName);

        VelocityContext context = GenUtils.getVelocityContext(author, realPackageName, table);

        // 获取模板列表
        List<String> templates = GenUtils.getTemplates();
        log.info("templates:{}", templates.toString());
        for (String template : templates) {
            // 渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, CharsetKit.UTF_8);
            tpl.merge(context, sw);
            try {
                log.info("realPackageName:{},template:{},moduleName:{}", realPackageName, template, moduleName);
                // 添加到zip
                String fileName = GenUtils.getFileName(realPackageName, template, table, moduleName);
                log.info("fileName:{}", fileName);
                zip.putNextEntry(new ZipEntry(fileName));
                IOUtils.write(sw.toString(), zip, CharsetKit.UTF_8);
                IOUtils.closeQuietly(sw);
                zip.closeEntry();
            } catch (Exception e) {
                log.error("渲染模板失败，表名: {}", table.getTableName(), e);
            }
        }
        log.info("渲染模成功，表名：{}", table.getTableName());
    }
}
