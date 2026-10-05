# Perguntas frequentes

**O app não reconhece o boot patcheado?**
Confira a SuperKey em Ajustes > SuperKey (tem que ser a mesma do patch). Sem ela, repatcheie com a chave atual.

**Bootloop?**
Segure volume - na inicialização (modo seguro pula os módulos) ou regrave o `boot.img` original via fastboot. O app faz backup automático antes de cada patch.

**Banco detectando root?**
Use perfis/exclusões + módulos de hide (ex: Peekaboo). Bootloader aberto e integridade forte têm limites intransponíveis com root — ver seção de bancos no README.

**Esqueci o PIN do app?**
Reinstale o app (o PIN é local e vai junto).
