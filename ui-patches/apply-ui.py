#!/usr/bin/env python3
"""Aplica a UI custom do MrEzequielSU sobre o fonte (pos-rebrand).
Uso: apply-ui.py <raiz-apatch-src> <dir-ui-patches>
Idempotente: aborta se algum anchor nao bater exatamente 1 vez.
"""
import shutil
import sys
from pathlib import Path

SRC = Path(sys.argv[1])
PATCH = Path(sys.argv[2])


def rep(path: Path, old: str, new: str, tag: str) -> None:
    s = path.read_text()
    c = s.count(old)
    assert c == 1, "%s: anchor %dx (esperava 1x) em %s" % (tag, c, path)
    path.write_text(s.replace(old, new, 1))
    print("%s ok" % tag)


THEME_DIR = SRC / "app/src/main/java/com/mrezequiel/su/ui/theme"
SCREEN_DIR = SRC / "app/src/main/java/com/mrezequiel/su/ui/screen"
assert THEME_DIR.is_dir(), "rode apos o rebrand (pacote novo nao existe)"

# 1) arquivos novos
shutil.copy(PATCH / "theme/MascotTheme.kt", THEME_DIR / "MascotTheme.kt")
shutil.copy(PATCH / "theme/ThemedBackground.kt", THEME_DIR / "ThemedBackground.kt")
shutil.copy(PATCH / "screen/BackgroundSettings.kt", SCREEN_DIR / "BackgroundSettings.kt")
print("arquivos copiados")

# 2) Theme.kt: registra o tema mascote + fundo global
theme = THEME_DIR / "Theme.kt"
rep(theme, "                else -> DarkBlueTheme",
    '                "mrezequiel" -> DarkMascotTheme\n                else -> DarkBlueTheme',
    "tema-dark")
rep(theme,
    '                "yellow" -> LightYellowTheme\n                else -> LightBlueTheme',
    '                "yellow" -> LightYellowTheme\n'
    '                "mrezequiel" -> LightMascotTheme\n                else -> LightBlueTheme',
    "tema-light")
rep(theme,
    "        content = {\n"
    "            MonetColorsProvider.UpdateCss()\n"
    "            content()\n"
    "        }",
    "        content = {\n"
    "            MonetColorsProvider.UpdateCss()\n"
    "            ThemedBackground { content() }\n"
    "        }",
    "fundo-global")

# 3) Settings.kt: entra o mascote na lista + linha do fundo antes de idioma
settings = SCREEN_DIR / "Settings.kt"
rep(settings, '        APColor("yellow", R.string.yellow_theme),',
    '        APColor("yellow", R.string.yellow_theme),\n'
    '        APColor("mrezequiel", R.string.mrezequiel_theme),',
    "lista-temas")
rep(settings, "            // language",
    "            BackgroundSettingRow()\n\n            // language",
    "linha-fundo")

# 4) strings.xml
strings = SRC / "app/src/main/res/values/strings.xml"
rep(strings, "</resources>",
    '    <string name="mrezequiel_theme">Mascote</string>\n'
    '    <string name="bg_title">Fundo personalizado</string>\n'
    '    <string name="bg_off">Desligado</string>\n'
    '    <string name="bg_color">Cor sólida</string>\n'
    '    <string name="bg_photo">Foto / GIF</string>\n'
    '    <string name="bg_pick">Escolher imagem</string>\n'
    "</resources>",
    "strings")

# 5) coil-gif (fundo animado)
toml = SRC / "gradle/libs.versions.toml"
rep(toml,
    'io-coil-kt-coil3-coil-compose = { group = "io.coil-kt.coil3", name = "coil-compose", version.ref = "coil3" }',
    'io-coil-kt-coil3-coil-compose = { group = "io.coil-kt.coil3", name = "coil-compose", version.ref = "coil3" }\n'
    'io-coil-kt-coil3-coil-gif = { group = "io.coil-kt.coil3", name = "coil-gif", version.ref = "coil3" }',
    "toml-gif")
gradle = SRC / "app/build.gradle.kts"
rep(gradle, "implementation(libs.io.coil.kt.coil3.coil.compose)",
    "implementation(libs.io.coil.kt.coil3.coil.compose)\n"
    "    implementation(libs.io.coil.kt.coil3.coil.gif)",
    "gradle-gif")

print("UI custom aplicada")
