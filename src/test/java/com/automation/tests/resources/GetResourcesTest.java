package com.automation.tests.resources;

import com.automation.api.models.Resource;
import com.automation.api.models.ResourceListResponse;
import com.automation.base.BaseTest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.*;

@Epic("Resource Management")
@Feature("Get Resources")
public class GetResourcesTest extends BaseTest {

    @Test(groups = {"smoke", "regression", "resources"})
    @Description("Verify getting list of resources with default page")
    @Story("Get resources list - default page")
    public void testGetResourcesDefaultPage() {
        Response response = resourceEndpoints.getResources();
        
        response.then()
                .statusCode(HttpStatus.SC_OK)
                .body("page", equalTo(1))
                .body("per_page", equalTo(6))
                .body("total", greaterThan(0))
                .body("total_pages", greaterThan(0))
                .body("data.size()", equalTo(6))
                .body("data[0].id", notNullValue())
                .body("data[0].name", notNullValue())
                .body("data[0].year", notNullValue())
                .body("data[0].color", notNullValue())
                .body("data[0].pantone_value", notNullValue())
                .body("support.url", notNullValue())
                .body("support.text", notNullValue());
    }

    @Test(groups = {"smoke", "regression", "resources"})
    @Description("Verify getting list of resources with specific page")
    @Story("Get resources list - specific page")
    public void testGetResourcesSpecificPage() {
        int page = 2;
        Response response = resourceEndpoints.getResources(page);
        
        response.then()
                .statusCode(HttpStatus.SC_OK)
                .body("page", equalTo(page))
                .body("per_page", equalTo(6))
                .body("data.size()", equalTo(6));
    }

    @Test(groups = {"regression", "resources"})
    @Description("Verify getting single resource by ID")
    @Story("Get single resource")
    public void testGetSingleResource() {
        int resourceId = 2;
        Response response = resourceEndpoints.getResource(resourceId);
        
        response.then()
                .statusCode(HttpStatus.SC_OK)
                .body("data.id", equalTo(resourceId))
                .body("data.name", equalTo("fuchsia rose"))
                .body("data.year", equalTo(2001))
                .body("data.color", equalTo("#C74375"))
                .body("data.pantone_value", equalTo("17-2031"))
                .body("support.url", notNullValue())
                .body("support.text", notNullValue());
    }

    @Test(groups = {"regression", "resources"})
    @Description("Verify getting non-existent resource returns 404")
    @Story("Get non-existent resource")
    public void testGetNonExistentResource() {
        int resourceId = 999;
        Response response = resourceEndpoints.getResource(resourceId);
        
        response.then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test(groups = {"regression", "resources"})
    @Description("Verify getting resources as typed object")
    @Story("Typed response")
    public void testGetResourcesAsObject() {
        ResourceListResponse resourceList = resourceEndpoints.getResourcesAsObject();
        
        Assert.assertEquals(resourceList.getPage(), 1);
        Assert.assertEquals(resourceList.getPerPage(), 6);
        Assert.assertTrue(resourceList.getTotal() > 0);
        Assert.assertTrue(resourceList.getTotalPages() > 0);
        Assert.assertEquals(resourceList.getData().size(), 6);
        Assert.assertNotNull(resourceList.getSupport());
        
        Resource firstResource = resourceList.getData().get(0);
        Assert.assertNotNull(firstResource.getId());
        Assert.assertNotNull(firstResource.getName());
        Assert.assertNotNull(firstResource.getYear());
        Assert.assertNotNull(firstResource.getColor());
        Assert.assertNotNull(firstResource.getPantoneValue());
    }

    @Test(groups = {"regression", "resources"})
    @Description("Verify getting single resource as typed object")
    @Story("Typed response - single resource")
    public void testGetSingleResourceAsObject() {
        Resource resource = resourceEndpoints.getResourceAsObject(2);
        
        Assert.assertEquals(resource.getId(), Integer.valueOf(2));
        Assert.assertEquals(resource.getName(), "fuchsia rose");
        Assert.assertEquals(resource.getYear(), Integer.valueOf(2001));
        Assert.assertEquals(resource.getColor(), "#C74375");
        Assert.assertEquals(resource.getPantoneValue(), "17-2031");
    }
}
