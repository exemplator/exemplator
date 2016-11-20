package xyz.exemplator.exemplator.data;

import org.apache.http.HttpException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public interface ICodeSearch {
    CompletableFuture<List<Optional<CodeSample>>> fetch(List<String> searchTerms, int page,
                                                        ExecutorService executorService) throws HttpException;
}