package xyz.exemplator.exemplator.data;

import org.apache.http.HttpException;

import java.util.List;

public interface ICodeSearch {
    List<CodeSample> fetch(List<String> searchTerms, int page) throws HttpException;
}
