package com.wash.recover;


import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wash.entity.constants.FilesEnum;
import com.wash.entity.franchisee.FranchiseeTb;
import com.wash.mapper.FranchiseeSiteTbMapper;
import com.wash.mapper.FranchiseeTbMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

//基于一天的融合错误恢复前一天的
@Component
public class RecoverByOneDay {
    public  static final String FILE_PATH = "D:\\mogo\\wash\\";

    @Autowired
    private FranchiseeTbMapper franchiseeTbMapper;

    public  void reco() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(FILE_PATH + FilesEnum.SCH.getFileName()));

        for(String line:lines){
            if(!line.contains("20250416")){
                continue;
            }
            String[] split = line.split(",");
            if(!line.contains("20250416")){
                continue;
            }
            if(split.length<6){
                continue;
            }
            if(line.contains("all")){
                continue;
            }
            int ven=Integer.valueOf(split[1]);
            int incom=Integer.valueOf(split[2])*10;
            int date=Integer.valueOf(split[0]);

            if(date!=20250416){
                continue;
            }

            UpdateWrapper<FranchiseeTb> franchiseeTbUpdateWrapper = new UpdateWrapper<>();
            franchiseeTbUpdateWrapper.eq("id", ven)
                    .setSql("settled_amount = settled_amount+" + incom)
                    .setSql("wait_withdraw = wait_withdraw+" + incom)
                    .setSql("stmt_recharge_amount = stmt_recharge_amount+" +incom)
                    .setSql("stmt_profit_amount = stmt_profit_amount+" + incom);
            franchiseeTbMapper.update(null, franchiseeTbUpdateWrapper);
        }

    }

}
