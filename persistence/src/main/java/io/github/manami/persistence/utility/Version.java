package io.github.manami.persistence.utility;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Optional;
import lombok.Data;
import org.apache.commons.lang3.math.NumberUtils;

public class Version {

  private static final String MSG_INVALID_VERSION = "Version is not valid.";

  /**
   * A Version consists of three parts: major, minor, bugfix.
   */
  private static final int VERSION_PARTS = 3;
  private final SplitVersion internalVersion;


  public Version(final String version) {
    if (!isValid(version)) {
      throw new IllegalArgumentException(MSG_INVALID_VERSION);
    }

    internalVersion = extractVersionParts(version).get();
  }


  public boolean isOlderThan(final String otherVersion) {
    return !isNewerThan(otherVersion);
  }


  public boolean isNewerThan(final String otherVersion) {
    if (!isValid(otherVersion)) {
      throw new IllegalArgumentException(MSG_INVALID_VERSION);
    }

    final SplitVersion otherSplitVersion = extractVersionParts(otherVersion).get();

    if (internalVersion.getMajor() > otherSplitVersion.getMajor()) {
      return true;
    }

    if (internalVersion.getMajor() < otherSplitVersion.getMajor()) {
      return false;
    }

    if (internalVersion.getMinor() > otherSplitVersion.getMinor()) {
      return true;
    }

    if (internalVersion.getMinor() < otherSplitVersion.getMinor()) {
      return false;
    }

    if (internalVersion.getBugfix() > otherSplitVersion.getBugfix()) {
      return true;
    }

    return false;
  }


  private static Optional<SplitVersion> extractVersionParts(final String version) {
    final String[] splitParts = version.split("\\.");

    if (splitParts.length != VERSION_PARTS) {
      return Optional.empty();
    }

    for (int i = 0; i < splitParts.length; i++) {
      if (!NumberUtils.isParsable(splitParts[i])) {
        return Optional.empty();
      }
    }

    final SplitVersion ret = new SplitVersion();
    ret.setMajor(Integer.parseInt(splitParts[0]));
    ret.setMinor(Integer.parseInt(splitParts[1]));
    ret.setBugfix(Integer.parseInt(splitParts[2]));

    return Optional.ofNullable(ret);
  }


  public static boolean isValid(final String version) {
    Optional<SplitVersion> splitVersion = Optional.empty();

    if (isNotBlank(version)) {
      splitVersion = extractVersionParts(version);
    }

    return splitVersion.isPresent();
  }

  @Data
  private static final class SplitVersion {

    private int major = 0;
    private int minor = 0;
    private int bugfix = 0;
  }
}
