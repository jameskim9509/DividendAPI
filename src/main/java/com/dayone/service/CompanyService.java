package com.dayone.service;

import com.dayone.exception.impl.NoCompanyException;
import com.dayone.exception.impl.NoScrapedCompany;
import com.dayone.model.Company;
import com.dayone.model.Dividend;
import com.dayone.persist.CompanyRepository;
import com.dayone.persist.DividendRepository;
import com.dayone.persist.entity.CompanyEntity;
import com.dayone.persist.entity.DividendEntity;
import com.dayone.scraper.Scraper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class CompanyService {

    private final Trie trie;
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;
    private final Scraper yahooScraper;


    public Company save(String ticker) {
        return this.storeCompanyAndDividend(ticker);
    }

    public Page<CompanyEntity> getAllCompany(Pageable pageable) {
        return companyRepository.findAll(pageable);
    }

    private Company storeCompanyAndDividend(String ticker) {
        // 1. ticker 를 기준으로 회사를 스크래핑
        boolean exists = companyRepository.existsByTicker(ticker);
        if(!exists) {
            Company company = yahooScraper.scrapCompanyByTicker(ticker);

            if(ObjectUtils.isEmpty(company))
                throw new NoScrapedCompany();

            companyRepository.save(new CompanyEntity(company));
        }

        // 2. 해당 회사가 존재할 경우, 회사의 배당금 정보를 스크래핑
        CompanyEntity findCompanyEntity =
                companyRepository.findByTicker(ticker).get();
        Company findCompany = new Company(
                findCompanyEntity.getTicker(), findCompanyEntity.getName()
        );

        var scrapedResult = yahooScraper.scrap(findCompany);
        List<Dividend> dividendList = scrapedResult.getDividends();
        dividendList.stream()
                .map(e -> new DividendEntity(findCompanyEntity.getId(), e))
                .forEach(e ->
                {
                    if(!dividendRepository
                            .existsByCompanyIdAndDate(findCompanyEntity.getId(), e.getDate()))
                        dividendRepository.save(e);
                }
    );

        // 3. 스크래핑 결과 반환
        return findCompany;
    }

    public List<String> getCompanyNamesByKeyword(String keyword) {
        return autocomplete(keyword);
    }

    public void addAutocompleteKeyword(String keyword) {
        this.trie.put(keyword, null);
    }

    public List<String> autocomplete(String keyword) {
        return (List<String>) this.trie.prefixMap(keyword).keySet()
                .stream()
                .limit(10)
                .collect(Collectors.toList());
    }

    public void deleteAutocompleteKeyword(String keyword) {
        this.trie.remove(keyword);
    }

    public String deleteCompany(String ticker) {
        // 1. 배당금 정보 삭제
        CompanyEntity findCompanyEntity
                = companyRepository.findByTicker(ticker)
                    .orElseThrow(() -> new NoCompanyException());

        List<DividendEntity> dividendEntityList
                = dividendRepository.findAllByCompanyId(findCompanyEntity.getId());
        dividendEntityList.stream()
                .forEach(e -> dividendRepository.delete(e));

        // 2. 회사 정보 삭제
        companyRepository.delete(findCompanyEntity);

        deleteAutocompleteKeyword(findCompanyEntity.getName());

        return findCompanyEntity.getName();
    }

}
