package org.tis.tools.senior.module.developer.entity.vo;

import lombok.Data;
import org.tis.tools.senior.module.developer.entity.enums.CommitType;
import org.tis.tools.senior.module.developer.util.DeveloperUtils;

import java.util.Date;

/**
 * description:
 *
 * @author zhaoch
 * @date 2018/6/22
 **/
@Data
public class SvnFile {

    private Long revision;

    private String author;

    private Date data;

    private CommitType type;

    private String path;

    private String nodeType;

    private String programName;

    private String projectName;

    public String getProgramName() {
        return DeveloperUtils.getProgramName(this.path);
    }

}
