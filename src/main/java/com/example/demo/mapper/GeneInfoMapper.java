package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.demo.dto.GeneQueryDTO;
import com.example.demo.entity.GeneInfo;
import com.example.demo.mapper.provider.GeneSqlProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

@Mapper
public interface GeneInfoMapper extends BaseMapper<GeneInfo> {
    @Select("<script>" +
            "SELECT Gene, Name, Chromosome, Start, End, Protein, Product, Description " +
            "FROM gene_info WHERE Gene IN " +
            "<foreach item='item' collection='genes' open='(' separator=',' close=')'>" +
            "   #{item}" +
            "</foreach>" +
            "</script>")
    List<GeneInfo> batchSearchByGenes(@Param("genes") List<String> genes);

    @SelectProvider(type = GeneSqlProvider.class, method = "searchAllGenes")
    List<GeneInfo> searchAllGenes(@Param("query") GeneQueryDTO query);

    IPage<GeneInfo> searchGenes(IPage<GeneInfo> page, @Param("query") GeneQueryDTO query);
}
