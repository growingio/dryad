import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import sbt._

import scalariform.formatter.preferences.FormattingPreferences

object Formatting {
  lazy val formatSettings = SbtScalariform.scalariformSettings ++ Seq(
    ScalariformKeys.preferences in Compile := formattingPreferences,
    ScalariformKeys.preferences in Test := formattingPreferences
  )

  lazy val docFormatSettings = SbtScalariform.scalariformSettings ++ Seq(
    ScalariformKeys.preferences in Compile := docFormattingPreferences,
    ScalariformKeys.preferences in Test := docFormattingPreferences
  )

  def formattingPreferences: FormattingPreferences = {
    import scalariform.formatter.preferences._
    FormattingPreferences()
      .setPreference(RewriteArrowSymbols, true)
      .setPreference(AlignParameters, true)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(DoubleIndentClassDeclaration, true)
  }

  def docFormattingPreferences: FormattingPreferences = {
    import scalariform.formatter.preferences._
    FormattingPreferences()
      .setPreference(RewriteArrowSymbols, true)
      .setPreference(AlignParameters, true)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(DoubleIndentClassDeclaration, true)
  }
}