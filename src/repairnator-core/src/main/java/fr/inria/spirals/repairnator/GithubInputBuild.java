package fr.inria.spirals.repairnator;

import java.util.ArrayList;
import java.util.List;

public class GithubInputBuild implements InputBuild {

    private String url;
    private String branch;
    private String sha;

    public GithubInputBuild(String url, String branch, String sha)
    {
        this.url = url;
        this.sha = sha;
        this.branch = branch;
    }

    public String getSha() {
        return sha;
    }

    public String getUrl() {
        return url;
    }

    public String getBranch() {
        return branch;
    }

    public String getSlug() {
        return url.split("https://github.com/",2)[1];
    }

    @Override
    public List<String> getEnvVariables() {
        List<String> r = new ArrayList<>();

        r.add("GITHUB_URL=" + url);
        r.add("GITHUB_BRANCH=" + sha);
        r.add("GITHUB_SHA=" + sha);
        return r;
    }

    @Override
    public String toString() {
        return url + "-" + branch + "-" + sha;
    }
}
