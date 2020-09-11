package com.sue.generate.controller;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.sue.generate.service.IGenService;
import org.springframework.web.bind.annotation.RestController;

/**
 * 代码生成 操作处理
 *
 * @author sue
 */
@Api(tags = "tool" , description = "代码生成工具")
@RestController
@RequestMapping("/tool/gen")
public class GenController {

    @Autowired
    private IGenService genService;


    /**
     * 生成代码
     */
    @ApiOperation(notes = "多表代码生成接口" , value = "")
    @GetMapping("/genCode/{author}/{packageName}/{dataBaseName}/{tableName}")
    public void genCode(HttpServletResponse response,
                        @PathVariable("author") String author,
                        @PathVariable("packageName") String packageName,
                        @PathVariable("dataBaseName") String dataBaseName,
                        @PathVariable("tableName") String tableName) throws IOException {
        byte[] data = genService.generatorCode(author, packageName, dataBaseName, tableName);
        genCode(response, data);
    }

    /**
     * 生成zip文件
     *
     * @param response
     * @param data
     * @throws IOException
     */
    private void genCode(HttpServletResponse response, byte[] data) throws IOException {
        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=\"sue.zip\"");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");
        IOUtils.write(data, response.getOutputStream());
    }
}
