/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.service;

import java.util.Set;

import org.obiba.opal.core.crypt.StudyKeyStore;

/**
 * Manage KeyStores.
 */
public interface StudyKeyStoreService {

  public static final String DEFAULT_STUDY_ID = "DEFAULT_STUDY_ID";

  /**
   * Returns a Set of study ids.
   * @return Study ids for studies managed by Opal.
   */
  public Set<String> getStudyIds();

  /**
   * Gets the {@link StudyKeyStore} for the study identified by the provided studyId.
   * @param studyId uniquely identifies a study.
   * @return A StudyKeyStore which allows management of the associated KeyStore.
   */
  public StudyKeyStore getStudyKeyStore(String studyId);

  /**
   * Save the provided {@link StudyKeyStore}. This will presist any updates made to the KeyStore.
   * @param studyKeyStore The StudyKeyStore to save.
   */
  public void saveStudyKeyStore(StudyKeyStore studyKeyStore);

  /**
   * Creates a new key or updates an existing key. It is the responsibility of the client to ensure that the caller
   * actually wishes to override an existing key.
   * @param alias name of the key
   * @param algorithm key algorithm (e.g. RSA, DSA)
   * @param size of the key
   * @param certificateInfo Certificate attributes as a String (e.g. CN=Administrator, OU=Bioinformatics, O=GQ,
   * L=Montreal, ST=Quebec, C=CA)
   */
  public void createOrUpdateKey(String alias, String algorithm, int size, String certificateInfo);

}
