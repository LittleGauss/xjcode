package org.xj_service.oa.service;

import org.xj_service.oa.entity.AdministrativeNotice;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 杜泽淇
 * @since 2025-08-13
 */
public interface IAdministrativeNoticeService extends IService<AdministrativeNotice> {

	/**
	 * 导出行政公示到给定的输出流（实现可以写入 Excel 内容）
	 */
	void exportNotices(java.io.ByteArrayOutputStream outputStream) throws Exception;

	/**
	 * 从上传文件批量导入行政公示（例如 Excel）
	 */
	void importNotices(org.springframework.web.multipart.MultipartFile file) throws Exception;

}
