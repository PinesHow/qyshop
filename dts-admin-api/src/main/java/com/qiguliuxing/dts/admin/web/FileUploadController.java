package com.qiguliuxing.dts.admin.web;

import com.qiguliuxing.dts.core.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.annotation.MultipartConfig;
import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

/**
 * 文件上传Controller
 */
@SuppressWarnings("unchecked")
@Controller
@MultipartConfig
@RequestMapping("/fileUpload")
public class FileUploadController {

    @Value("${file.post}")  //获取项目端口号
    private String post;

    @Value("${file.rootPath}")
    private String rootPath;  //文件保存根目录

//    @Value("${file.localPath}")
//    private String rootPath; //本地文件保存根目录，本地测试用

    @Value("${file.sonPath}")
    private String sonPath;  //文件保存子目录

    @Value("${file.host}")
    private String host;    //获取服务器ip

    @Value("${file.url}")
    private String fileUrl;   //项目域名

    @Value("${file.project}")
    private String project;

//    public String upload(HttpServletRequest request) {
//        if (request instanceof MultipartHttpServletRequest) {
//            // process
//            File dir = new File(rootPath + sonPath);
//            if (!dir.isDirectory())
//                dir.mkdir();
//            String fileOriginalName = file.getOriginalFilename();
//            String newFileName = UUID.randomUUID() + fileOriginalName.substring(fileOriginalName.lastIndexOf("."));
////        File writeFile = new File(rootPath + sonPath + newFileName);
////        //文件写入磁盘
////        file.transferTo(writeFile);
//            //返回存储的相对路径+文件名称
//            FileOutputStream out = new FileOutputStream(rootPath + sonPath + newFileName);
//            out.write(file.getBytes());
//            File uploadFile = new File(rootPath + sonPath+"/"+newFileName);
//            String uploadFileUrl = ("http://" + host + ":" + post + sonPath + newFileName);
//            if (!uploadFile.exists()){
//                bgyResult.setMsg("fail");
//                bgyResult.setData("文件上传失败！请刷新后重新上传！");
//            }else{
//                bgyResult.setCode(uploadFileUrl);
//                bgyResult.setMsg("ok");
//                bgyResult.setData("文件上传成功！");
//            }
//            out.close();
//            return  json.toJSONString(bgyResult);
//        }
//    }


    /**
     * 文件上传
     * @param file      MultipartFile 类
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public Object upload(@RequestParam("file") MultipartFile file) throws Exception {
        File dir = new File(rootPath + sonPath);
        if (!dir.isDirectory()){
            dir.mkdir();
        }
        String fileOriginalName = file.getOriginalFilename();
        String newFileName = UUID.randomUUID() + fileOriginalName.substring(fileOriginalName.lastIndexOf("."));
//        File writeFile = new File(rootPath + sonPath + newFileName);
//        //文件写入磁盘
//        file.transferTo(writeFile);
        //返回存储的相对路径+文件名称
//        System.out.println("sasasasa: "+rootPath+sonPath);
        FileOutputStream out = new FileOutputStream(rootPath + sonPath + newFileName);
        out.write(file.getBytes());
        File uploadFile = new File(rootPath + sonPath + newFileName);
        String uploadFileUrl = ("https://" + fileUrl + project+ "/" +  newFileName);//服务器域名
//        String uploadFileUrl = ("http://" + host + ":" + post + "/demo/" + newFileName); //测试上传文件地址
        out.close();
        if (!uploadFile.exists()){
            return ResponseUtil.fail(205,"文件上传失败！请刷新后重新上传！(允许上传的图片最大为10M)");
        }else{
            return ResponseUtil.fail(200,uploadFileUrl);
        }
    }




    /**
     * 文件上传
     * @param files      MultipartFile 类
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/batchUpload", method = RequestMethod.POST)
    @ResponseBody
    public Object batchUpload(@RequestParam("files") MultipartFile[] files) throws Exception {
        File dir = new File(rootPath + sonPath);
        if (!dir.isDirectory()){
            dir.mkdir();
        }
        String str = "";
        for(MultipartFile file : files){
            String fileOriginalName = file.getOriginalFilename();
            String newFileName = UUID.randomUUID() + fileOriginalName.substring(fileOriginalName.lastIndexOf("."));
            //返回存储的相对路径+文件名称
            FileOutputStream out = new FileOutputStream(rootPath + sonPath + newFileName);
            out.write(file.getBytes());
            File uploadFile = new File(rootPath + sonPath+"/"+newFileName);
            String uploadFileUrl = ("http://" + host + ":" + post + rootPath + sonPath + newFileName);
            str += uploadFileUrl;
            out.close();
        }
        return ResponseUtil.fail(205,str);
    }


//    @RequestMapping(value = "uploadImg", method = RequestMethod.POST)
//    @ResponseBody
//    public String uploadImg(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws Exception {
//        File dir = new File(rootPath + sonPath);     //文件上传存放目标文件夹
//        if (!dir.isDirectory()) {
//            dir.mkdir();
//        }
//        String originalFileName = file.getOriginalFilename();   //获取文件名
//        String newFileName = UUID.randomUUID() + originalFileName.substring(originalFileName.lastIndexOf("."));   //构建新的文件名
////        File writeFile = new File(rootPath + sonPath + newFileName);
////        //文件写入磁盘
////        file.transferTo(writeFile);
//        FileOutputStream out = new FileOutputStream(rootPath + sonPath + newFileName);
//        out.write(file.getBytes());
//        //返回存储的相对路径+文件名称
//        File uploadFile = new File( rootPath + sonPath+"/"+newFileName);//"/tmp/tomcat/work/Tomcat/localhost/ROOT/" +
//        String uploadFileUrl = ("http://" + host + ":" + post + "/images/" + newFileName);  //Linux
//
//        //文件上传成功后，将文件（路径+文件）保存到session中
//        HttpSession session = request.getSession();
//        Map<String, Object> map = new HashMap<>();
//        if (!uploadFile.exists()) {
//            bgyResult.setMsg("fail");
//            bgyResult.setData("文件上传失败！请刷新后重新上传！");
//        } else {
//            session.setAttribute("uploadFileUrl", uploadFileUrl);
//            bgyResult.setCode("200");     //code为0表示成功   其它表示失败
//            bgyResult.setMsg("ok");     //提示信息    ---一般用于上传失败
//            map.put("src", uploadFileUrl);    //设置图片回调显示url
//            map.put("title", newFileName);    //图片名称，这个会显示在输入框里
//            bgyResult.setData(map);
//        }
//        return json.toJSONString(bgyResult);
//
//    }
}
