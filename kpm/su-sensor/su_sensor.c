/* SPDX-License-Identifier: GPL-2.0-or-later */
/*
 * su_sensor: avisa quando algum app executa o binario su.
 * Inspirado no exemplo hosts_file_redirect (bmax121/skkk, GPL-2.0).
 * Parte do projeto MrEzequielSU.
 */

#include <linux/cred.h>
#include <linux/err.h>
#include <linux/fs.h>
#include <linux/kernel.h>
#include <linux/printk.h>
#include <linux/sched.h>
#include <linux/string.h>

#include <kpm_utils.h>
#include <kpm_hook_utils.h>

KPM_NAME("su_sensor");
KPM_VERSION("1.0.0");
KPM_LICENSE("GPL v2");
KPM_AUTHOR("EzequielDevTeam");
KPM_DESCRIPTION("su exec sensor: loga MRESU su-req uid/comm");

struct open_flags;
hook_func_def(do_filp_open, struct file *, int dfd, struct filename *pathname, const struct open_flags *o);
hook_func_no_info(do_filp_open);

static bool is_su_path(const char *name)
{
    size_t n;

    if (!name)
        return false;
    n = strlen(name);
    if (n < 3)
        return false;
    return name[n - 3] == '/' && name[n - 2] == 's' && name[n - 1] == 'u';
}

static struct file *hook_replace(do_filp_open)(int dfd, struct filename *pathname, const struct open_flags *o)
{
    if (pathname && pathname->name && is_su_path(pathname->name)) {
        pr_info("MRESU su-req uid=%u path=%s\n", current_uid(), pathname->name);
    }
    return hook_call_backup(do_filp_open, dfd, pathname, o);
}

static long su_sensor_init(const char *args, const char *event, void *__user reserved)
{
    if (!hook_success(do_filp_open)) {
        hook_install(do_filp_open);
        if (!hook_success(do_filp_open)) {
            pr_info("MRESU: hook falhou!\n");
            return 1;
        }
    }
    pr_info("MRESU: sensor ligado!\n");
    return 0;
}

static long su_sensor_control0(const char *args, char *__user out_msg, int outlen)
{
    if (args) {
        if (strncmp(args, "ping", 4) == 0) {
            writeOutMsg(out_msg, &outlen, "MRESU: pong !");
            return 0;
        }
    }
    return -1;
}

static long su_sensor_exit(void *__user reserved)
{
    if (hook_success(do_filp_open)) {
        unhook((void *)hook_original(do_filp_open));
        hook_err(do_filp_open) = HOOK_NOT_HOOK;
        pr_info("MRESU: sensor desligado !\n");
    }
    return 0;
}

KPM_INIT(su_sensor_init);
KPM_CTL0(su_sensor_control0);
KPM_EXIT(su_sensor_exit);
