package com.yixue.media.controller;

import com.yixue.media.model.dto.RestResponse;
import com.yixue.media.model.dto.UploadFileParamsDto;
import com.yixue.media.model.dto.UploadFileResultDto;
import com.yixue.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @author Tavis
 * @date 2023-08-15
 * @desc 视频文件控制器
 */
@Api(value = "视频文件上传接口", tags = "视频文件上传接口")
@RestController
public class VideoFilesController {

    @Autowired
    MediaFileService mediaFileService;

    @ApiOperation(value = "检查文件是否存在接口")
    @PostMapping("/upload/checkfile")
    public RestResponse<Boolean> checkfile(@RequestParam("fileMd5") String fileMd5) throws Exception {
        return mediaFileService.checkFile(fileMd5);
    }

    @ApiOperation(value = "检查分块是否存在接口")
    @PostMapping("/upload/checkchunk")
    public RestResponse<Boolean> checkchunk(@RequestParam("fileMd5") String fileMd5,
                                            @RequestParam("chunk") int chunk) throws Exception {
        return mediaFileService.checkChunk(fileMd5, chunk);
    }

    @ApiOperation(value = "上传视频文件接口")
    @PostMapping(value = "/upload/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RestResponse uploadvideo(@RequestPart("videofile") MultipartFile filedata) throws Exception {
        //上传文件的信息
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        //原始文件名称
        uploadFileParamsDto.setFilename(filedata.getOriginalFilename());
        //文件大小
        uploadFileParamsDto.setFileSize(filedata.getSize());
        //文件类型（001002是视频）
        uploadFileParamsDto.setFileType("001002");
        //创建一个临时文件
        File tempFile = File.createTempFile("minio", ".temp");
        filedata.transferTo(tempFile);
        //文件路径
        String localFilePath = tempFile.getAbsolutePath();
        //上传图片
        return mediaFileService.uploadVideo(uploadFileParamsDto, localFilePath);
    }

//    @ApiOperation(value = "上传分块文件接口")
//    @PostMapping("/upload/chunk")
//    public RestResponse uploadchunk(@RequestParam("file") MultipartFile file,
//                                    @RequestParam("fileMd5") String fileMd5,
//                                    @RequestParam("chunk") int chunk) throws Exception {
//        //创建一个临时文件
//        File tempFile = File.createTempFile("minio", "temp");
//        file.transferTo(tempFile);
//        //文件路径
//        String localFilePath = tempFile.getAbsolutePath();
//        RestResponse restResponse = mediaFileService.uploadChunk(fileMd5, chunk, localFilePath);
//        return restResponse;
//    }

    @ApiOperation(value = "合并分块文件接口")
    @PostMapping("/mergechunks")
    public RestResponse mergechunks(@RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("fileName") String fileName,
                                    @RequestParam("chunkTotal") int chunkTotal) throws Exception {
        Long companyId = 1232141425L;
        //文件信息对象
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFilename(fileName);
        uploadFileParamsDto.setTags("视频文件");
        //001002代表视频文件
        uploadFileParamsDto.setFileType("001002");
        return mediaFileService.mergechunks(companyId, fileMd5, chunkTotal, uploadFileParamsDto);

    }

}
