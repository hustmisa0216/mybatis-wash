package com.wash.service;

import com.wash.entity.Series;
import com.wash.entity.franchisee.FranchiseeSiteTb;
import com.wash.entity.statistics.FaSettlementTb;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Modifier {
    public void delete(String vendorId, FaSettlementTb faSettlementTbRes, FranchiseeSiteTb franchiseeSiteTb, List<Series> seriesList) {

        for(Series series:seriesList){

        }

    }
    public void update(String vendorId, FaSettlementTb faSettlementTbRes, FranchiseeSiteTb franchiseeSiteTb, List<Series> resSeries) {

    }
}
