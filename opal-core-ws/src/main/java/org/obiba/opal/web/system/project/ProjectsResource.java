/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.system.project;

import java.net.URI;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.obiba.magma.DuplicateDatasourceNameException;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.support.DatasourceParsingException;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.runtime.security.support.OpalPermissions;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.magma.support.NoSuchDatasourceFactoryException;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Projects;
import org.obiba.opal.web.security.AuthorizationInterceptor;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Component
@Path("/projects")
public class ProjectsResource {

  @Autowired
  private ProjectService projectService;

  @GET
  @Transactional(readOnly = true)
  @NoAuthorization
  public List<Projects.ProjectDto> getProjects() {
    List<Projects.ProjectDto> projects = Lists.newArrayList();
    for(Project project : projectService.getProjects()) {
      try {
        projects.add(Dtos.asDto(project, projectService.getProjectDirectoryPath(project)));
      } catch(NoSuchDatasourceException e) {
        // ignore
      }
    }
    return projects;
  }

  @POST
  public Response createProject(@Context UriInfo uriInfo, Projects.ProjectFactoryDto projectFactoryDto) {
    Response.ResponseBuilder response;
    try {

      Project project = Dtos.fromDto(projectFactoryDto);
      projectService.save(project);
      URI projectUri = uriInfo.getBaseUriBuilder().path("project").path(project.getName()).build();
      //Projects.ProjectDto projectDto = Dtos.asDto(project, projectService.getProjectDirectoryPath(project));
      response = Response.created(projectUri).header(AuthorizationInterceptor.ALT_PERMISSIONS,
          new OpalPermissions(projectUri, Opal.AclAction.PROJECT_ALL));

    } catch(NoSuchDatasourceFactoryException e) {
      response = Response.status(BAD_REQUEST)
          .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "UnidentifiedDatasourceFactory").build());
    } catch(DuplicateDatasourceNameException e) {
      response = Response.status(BAD_REQUEST)
          .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "DuplicateDatasourceName").build());
    } catch(DatasourceParsingException e) {
      response = Response.status(BAD_REQUEST)
          .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "DatasourceCreationFailed", e));
    } catch(MagmaRuntimeException e) {
      response = Response.status(BAD_REQUEST)
          .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "DatasourceCreationFailed", e));
    }
    return response.build();
  }

}