package xyz.exemplator.exemplator.data;

import org.apache.http.HttpException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public interface ICodeSearch {
    List<CompletableFuture<Optional<CodeSample>>> fetch(List<String> searchTerms, int page,
                                                        ExecutorService executorService) throws HttpException;

    Optional<CodeSample> fetchRawCode(CodeSample codeSample);

    Optional<CodeSample> fetchGithubRating(CodeSample codeSample);
}