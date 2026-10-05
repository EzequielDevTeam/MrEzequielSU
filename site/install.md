# Instalação

## Requisitos

- Bootloader desbloqueado, backup do `boot.img` original, bateria carregada.

## Passo a passo

1. Instale o APK da [Release](https://github.com/EzequielDevTeam/MrEzequielSU/releases).
2. Abra o app > **Patch**: selecione o `boot.img` original, defina a **SuperKey** (anote!) e patcheie.
3. Teste sem gravar: `fastboot boot mrezequielsu_patched_....img`.
4. Se ligou tudo: `fastboot flash boot` no slot ativo + reboot.
5. O app deve mostrar **instalado**. Se reinstalar o app depois, digite a mesma SuperKey em **Ajustes > SuperKey** (casa com o boot já patcheado).

## Sem PC (Direct Install)

Com root já ativo, o app patcheia e instala direto, sem computador.
