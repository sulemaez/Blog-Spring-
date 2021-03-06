package com.soyeyo.Blog.services;

import com.soyeyo.Blog.dto.admin.Link;
import com.soyeyo.Blog.dto.admin.PaginationDTO;
import com.soyeyo.Blog.models.Category;
import com.soyeyo.Blog.models.Post;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.regex.Pattern;

@Service
public class PaginationService{

    private static String url;

    private PaginationService(){}

    @Value(value = "${url.path}")
    public void setUrl(String newUrl){
       url = newUrl;
    }

    public  static <T> PaginationDTO getPagination(String page, String sort,String perPage,PagingAndSortingRepository repo, String model){
        if(url == null){
           new PaginationService();
        }
        //set page number
        int pageNo = Integer.parseInt(page);
        //per page
        int per_page = Integer.parseInt(perPage);
        //list to store posts
        Iterable<T>  list = null;

        //get sorting
        if(!sort.equals("")){
            //if sorting is set
            String params[] = sort.split(Pattern.quote("|"));

            Sort.Direction dir = Sort.Direction.ASC;
            if(params.length > 1 && params[1].contains("desc"))dir = Sort.Direction.DESC;
            list = repo.findAll(PageRequest.of(pageNo-1,per_page,Sort.by(dir,params[0])));

        }else{
            //if sorting not set
            list =  repo.findAll(PageRequest.of(pageNo-1,per_page));
        }

        //get total posts
        long total = repo.count();

        //set previous and next page
        String previous_url = null;
        String next_url;
        if(pageNo > 1){
            previous_url = url+"/"+model+"?sort="+sort+"&page="+(pageNo-1);
        }

        next_url = url+"/"+model+"?sort="+sort+"&page="+(pageNo+1);
        //get to and from posts
        int from = per_page * (pageNo-1) + 1;
        int to  = (from - 1 ) + ((Page<T>) list).getNumberOfElements();


        //get last page
        int last_page = (int ) Math.ceil((double)total /(double)per_page);

        PaginationDTO<T> paginDTO = new PaginationDTO<>();

        if(!model.equals("post")){
            paginDTO.setData(getData(list));
        }

        paginDTO.setLinks(new Link(total,from,to,next_url,previous_url,pageNo,last_page,per_page));
        return paginDTO;
    }

    public static <T> Iterable<T> getData(Iterable<T> list) {
        Iterable<T> finalList = new ArrayList<>();

        for (T t : list) {
             //remove the posts from category
             if(t instanceof Category){
                 ((Category) t).setPosts(new ArrayList<>());
             }else if(t instanceof Post){
                 ((Post) t).getCategory().setPosts(new ArrayList<>());
             }
            ((ArrayList<T>) finalList).add(t);
        }
        return finalList;
    }
}
